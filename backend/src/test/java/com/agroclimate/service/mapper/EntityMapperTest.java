package com.agroclimate.service.mapper;

import com.agroclimate.domain.entity.*;
import com.agroclimate.domain.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EntityMapperTest {

    private EntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EntityMapper();
    }

    @Test
    void toGreenhouseDto_mapsFields() {
        Greenhouse g = Greenhouse.builder()
                .id(10L)
                .name("GH-A")
                .location("North")
                .build();

        var dto = mapper.toGreenhouseDto(g);

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.name()).isEqualTo("GH-A");
        assertThat(dto.location()).isEqualTo("North");
    }

    @Test
    void toZoneDto_includesGreenhouseId() {
        Greenhouse gh = Greenhouse.builder().id(5L).name("G").location("L").build();
        Zone z = Zone.builder()
                .id(2L)
                .greenhouse(gh)
                .name("Zone 1")
                .description("d")
                .areaM2(new BigDecimal("12.5"))
                .build();

        var dto = mapper.toZoneDto(z);

        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.greenhouseId()).isEqualTo(5L);
        assertThat(dto.name()).isEqualTo("Zone 1");
        assertThat(dto.description()).isEqualTo("d");
        assertThat(dto.areaM2()).isEqualByComparingTo("12.5");
    }

    @Test
    void toSensorDto_includesMetricAndUnit() {
        Greenhouse gh = Greenhouse.builder().id(1L).name("g").location(null).build();
        Zone z = Zone.builder().id(3L).greenhouse(gh).name("z").description(null).areaM2(null).build();
        LocalDateTime installed = LocalDateTime.of(2024, 6, 1, 12, 0);
        Sensor s = Sensor.builder()
                .id(7L)
                .zone(z)
                .metric(Metric.TEMPERATURE)
                .name("T1")
                .status(SensorStatus.ACTIVE)
                .installedAt(installed)
                .build();

        var dto = mapper.toSensorDto(s);

        assertThat(dto.id()).isEqualTo(7L);
        assertThat(dto.zoneId()).isEqualTo(3L);
        assertThat(dto.metric()).isEqualTo(Metric.TEMPERATURE);
        assertThat(dto.unit()).isEqualTo("°C");
        assertThat(dto.name()).isEqualTo("T1");
        assertThat(dto.status()).isEqualTo(SensorStatus.ACTIVE);
        assertThat(dto.installedAt()).isEqualTo(installed);
    }

    @Test
    void toMeasurementDto_mapsValueAndSource() {
        Greenhouse gh = Greenhouse.builder().id(1L).name("g").location(null).build();
        Zone z = Zone.builder().id(1L).greenhouse(gh).name("z").description(null).areaM2(null).build();
        Sensor s = Sensor.builder()
                .id(4L)
                .zone(z)
                .metric(Metric.HUMIDITY)
                .name("H")
                .status(SensorStatus.ACTIVE)
                .build();
        LocalDateTime ts = LocalDateTime.of(2025, 1, 2, 3, 4, 5);
        Measurement m = Measurement.builder()
                .id(100L)
                .sensor(s)
                .ts(ts)
                .value(new BigDecimal("55.1"))
                .source(MeasurementSource.SIM)
                .build();

        var dto = mapper.toMeasurementDto(m);

        assertThat(dto.id()).isEqualTo(100L);
        assertThat(dto.sensorId()).isEqualTo(4L);
        assertThat(dto.metric()).isEqualTo(Metric.HUMIDITY);
        assertThat(dto.value()).isEqualByComparingTo("55.1");
        assertThat(dto.unit()).isEqualTo("%");
        assertThat(dto.ts()).isEqualTo(ts);
        assertThat(dto.source()).isEqualTo(MeasurementSource.SIM);
    }

    @Test
    void toRuleDto_mapsThresholds() {
        Greenhouse gh = Greenhouse.builder().id(1L).name("g").location(null).build();
        Zone z = Zone.builder().id(9L).greenhouse(gh).name("z").description(null).areaM2(null).build();
        ThresholdRule r = ThresholdRule.builder()
                .id(11L)
                .zone(z)
                .metric(Metric.LIGHT)
                .minValue(BigDecimal.ZERO)
                .maxValue(new BigDecimal("1000"))
                .enabled(false)
                .severity(Severity.WARN)
                .build();

        var dto = mapper.toRuleDto(r);

        assertThat(dto.id()).isEqualTo(11L);
        assertThat(dto.zoneId()).isEqualTo(9L);
        assertThat(dto.metric()).isEqualTo(Metric.LIGHT);
        assertThat(dto.unit()).isEqualTo("lx");
        assertThat(dto.minValue()).isEqualByComparingTo("0");
        assertThat(dto.maxValue()).isEqualByComparingTo("1000");
        assertThat(dto.enabled()).isFalse();
        assertThat(dto.severity()).isEqualTo(Severity.WARN);
    }

    @Test
    void toUserDto_collectsRoleCodes() {
        Role r1 = Role.builder().id(1L).code(RoleCode.ADMIN).name("a").description(null).build();
        Role r2 = Role.builder().id(2L).code(RoleCode.VIEWER).name("v").description(null).build();
        LocalDateTime created = LocalDateTime.of(2020, 1, 1, 0, 0);
        AppUser user = AppUser.builder()
                .id(42L)
                .email("u@test.local")
                .passwordHash("x")
                .fullName("User U")
                .status(UserStatus.ACTIVE)
                .createdAt(created)
                .roles(Set.of(r1, r2))
                .build();

        var dto = mapper.toUserDto(user);

        assertThat(dto.id()).isEqualTo(42L);
        assertThat(dto.email()).isEqualTo("u@test.local");
        assertThat(dto.fullName()).isEqualTo("User U");
        assertThat(dto.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(dto.createdAt()).isEqualTo(created);
        assertThat(dto.roles()).containsExactlyInAnyOrder("ADMIN", "VIEWER");
    }

    @Test
    void toAlertDto_buildsMessageAndHierarchyIds() {
        Greenhouse gh = Greenhouse.builder().id(1L).name("Теплица 1").location("Место").build();
        Zone z = Zone.builder().id(2L).greenhouse(gh).name("Секция А").description(null).areaM2(null).build();
        Sensor sensor = Sensor.builder()
                .id(3L)
                .zone(z)
                .metric(Metric.TEMPERATURE)
                .name("Датчик")
                .status(SensorStatus.ACTIVE)
                .build();
        ThresholdRule rule = ThresholdRule.builder()
                .id(4L)
                .zone(z)
                .metric(Metric.TEMPERATURE)
                .minValue(new BigDecimal("18"))
                .maxValue(new BigDecimal("26"))
                .enabled(true)
                .severity(Severity.CRITICAL)
                .build();
        BigDecimal last = new BigDecimal("30");
        Alert alert = Alert.builder()
                .id(99L)
                .rule(rule)
                .sensor(sensor)
                .startedAt(LocalDateTime.of(2025, 5, 1, 10, 0))
                .endedAt(null)
                .status(AlertStatus.OPEN)
                .lastValue(last)
                .message(null)
                .build();

        var dto = mapper.toAlertDto(alert);

        assertThat(dto.id()).isEqualTo(99L);
        assertThat(dto.ruleId()).isEqualTo(4L);
        assertThat(dto.sensorId()).isEqualTo(3L);
        assertThat(dto.metric()).isEqualTo(Metric.TEMPERATURE);
        assertThat(dto.severity()).isEqualTo(Severity.CRITICAL);
        assertThat(dto.status()).isEqualTo(AlertStatus.OPEN);
        assertThat(dto.lastValue()).isEqualByComparingTo("30");
        assertThat(dto.unit()).isEqualTo("°C");
        assertThat(dto.greenhouseId()).isEqualTo(1L);
        assertThat(dto.greenhouseName()).isEqualTo("Теплица 1");
        assertThat(dto.zoneId()).isEqualTo(2L);
        assertThat(dto.zoneName()).isEqualTo("Секция А");
        assertThat(dto.message()).contains("Температура").contains("30").contains("критично");
    }
}
