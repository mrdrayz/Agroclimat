package com.agroclimate.config;

import com.agroclimate.domain.entity.*;
import com.agroclimate.domain.enums.*;
import com.agroclimate.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Инициализирует справочные данные: роли, тестовый администратор, одна теплица,
 * зона, набор датчиков и пороговые правила. Запускается при пустой БД.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AppUserRepository userRepository;
    private final GreenhouseRepository greenhouseRepository;
    private final ZoneRepository zoneRepository;
    private final SensorRepository sensorRepository;
    private final ThresholdRuleRepository ruleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedAdmin();
        seedDemoData();
    }

    private void seedRoles() {
        for (RoleCode code : RoleCode.values()) {
            roleRepository.findByCode(code).orElseGet(() -> {
                Role r = Role.builder()
                        .code(code)
                        .name(humanize(code.name()))
                        .description("Role " + code.name())
                        .build();
                return roleRepository.save(r);
            });
        }
    }

    private void seedAdmin() {
        if (userRepository.existsByEmail("admin@agro.local")) return;

        Set<Role> roles = new HashSet<>();
        roleRepository.findByCode(RoleCode.ADMIN).ifPresent(roles::add);

        AppUser admin = AppUser.builder()
                .email("admin@agro.local")
                .passwordHash(passwordEncoder.encode("admin123"))
                .fullName("System Administrator")
                .status(UserStatus.ACTIVE)
                .roles(roles)
                .build();
        userRepository.save(admin);
        log.info("Created default admin: admin@agro.local / admin123");
    }

    private void seedDemoData() {
        if (greenhouseRepository.count() > 0) return;

        Greenhouse gh = greenhouseRepository.save(Greenhouse.builder()
                .name("Greenhouse #1")
                .location("North wing")
                .build());

        Zone zone = zoneRepository.save(Zone.builder()
                .greenhouse(gh)
                .name("Zone A")
                .description("Tomato section")
                .areaM2(new BigDecimal("120.50"))
                .build());

        // Набор датчиков на зону
        sensorRepository.saveAll(List.of(
                Sensor.builder().zone(zone).metric(Metric.TEMPERATURE).name("Temp-A1").status(SensorStatus.ACTIVE).build(),
                Sensor.builder().zone(zone).metric(Metric.HUMIDITY).name("Hum-A1").status(SensorStatus.ACTIVE).build(),
                Sensor.builder().zone(zone).metric(Metric.LIGHT).name("Light-A1").status(SensorStatus.ACTIVE).build()
        ));

        // Пороговые правила
        ruleRepository.saveAll(List.of(
                ThresholdRule.builder().zone(zone).metric(Metric.TEMPERATURE)
                        .minValue(new BigDecimal("18")).maxValue(new BigDecimal("28"))
                        .enabled(true).severity(Severity.WARN).build(),
                ThresholdRule.builder().zone(zone).metric(Metric.HUMIDITY)
                        .minValue(new BigDecimal("40")).maxValue(new BigDecimal("80"))
                        .enabled(true).severity(Severity.WARN).build(),
                ThresholdRule.builder().zone(zone).metric(Metric.LIGHT)
                        .minValue(new BigDecimal("3000")).maxValue(new BigDecimal("30000"))
                        .enabled(true).severity(Severity.INFO).build()
        ));

        log.info("Demo data created: greenhouse '{}', zone '{}', 3 sensors, 3 rules", gh.getName(), zone.getName());
    }

    private String humanize(String code) {
        return code.charAt(0) + code.substring(1).toLowerCase();
    }
}
