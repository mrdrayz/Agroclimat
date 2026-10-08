package com.agroclimate.service;

import com.agroclimate.domain.entity.AppUser;
import com.agroclimate.domain.entity.GreenhouseUser;
import com.agroclimate.domain.entity.Sensor;
import com.agroclimate.domain.entity.Zone;
import com.agroclimate.domain.enums.AccessLevel;
import com.agroclimate.domain.enums.RoleCode;
import com.agroclimate.dto.auth.GreenhouseAccessEntryDto;
import com.agroclimate.dto.auth.GreenhouseAccessSummaryDto;
import com.agroclimate.exception.NotFoundException;
import com.agroclimate.repository.GreenhouseUserRepository;
import com.agroclimate.repository.SensorRepository;
import com.agroclimate.repository.ZoneRepository;
import com.agroclimate.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Доступ к теплицам по таблице {@code greenhouse_user}.
 * Если у пользователя нет ни одной строки — действует только глобальная роль (как раньше).
 * Если есть строки — пользователь видит и (при MANAGE) изменяет только эти теплицы.
 */
@Service
@RequiredArgsConstructor
public class GreenhouseAccessService {

    private final GreenhouseUserRepository greenhouseUserRepository;
    private final ZoneRepository zoneRepository;
    private final SensorRepository sensorRepository;

    /** Для ответа /me без повторного обращения к SecurityContext. */
    @Transactional(readOnly = true)
    public GreenhouseAccessSummaryDto summaryFor(AppUser user) {
        boolean admin = user.getRoles().stream().anyMatch(r -> r.getCode() == RoleCode.ADMIN);
        if (admin) {
            return new GreenhouseAccessSummaryDto(false, List.of());
        }
        List<GreenhouseUser> links = greenhouseUserRepository.findAllByUserId(user.getId());
        if (links.isEmpty()) {
            return new GreenhouseAccessSummaryDto(false, List.of());
        }
        List<GreenhouseAccessEntryDto> entries = new ArrayList<>();
        for (GreenhouseUser gu : links) {
            entries.add(new GreenhouseAccessEntryDto(gu.getGreenhouse().getId(), gu.getAccessLevel().name()));
        }
        return new GreenhouseAccessSummaryDto(true, List.copyOf(entries));
    }

    @Transactional(readOnly = true)
    public AccessContext currentContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails user)) {
            throw new AccessDeniedException("Требуется вход в систему");
        }
        if (userHasRole(user, "ROLE_ADMIN")) {
            return new AccessContext(user, true, false, Map.of());
        }
        List<GreenhouseUser> links = greenhouseUserRepository.findAllByUserId(user.getId());
        if (links.isEmpty()) {
            return new AccessContext(user, false, false, Map.of());
        }
        Map<Long, AccessLevel> map = new HashMap<>();
        for (GreenhouseUser gu : links) {
            map.put(gu.getGreenhouse().getId(), gu.getAccessLevel());
        }
        return new AccessContext(user, false, true, Map.copyOf(map));
    }

    public void assertReadableGreenhouse(long greenhouseId) {
        AccessContext ctx = currentContext();
        if (!ctx.canReadGreenhouse(greenhouseId)) {
            throw new AccessDeniedException("Нет доступа к этой теплице");
        }
    }

    public void assertManageableGreenhouse(long greenhouseId) {
        AccessContext ctx = currentContext();
        if (!ctx.canManageGreenhouse(greenhouseId)) {
            throw new AccessDeniedException("Недостаточно прав для изменения этой теплицы");
        }
    }

    /** Создание новой теплицы: админ или агроном без явных назначений. */
    public void assertMayCreateGreenhouse() {
        AccessContext ctx = currentContext();
        if (ctx.admin()) {
            return;
        }
        if (ctx.scoped()) {
            throw new AccessDeniedException("При назначении на теплицы создание новых теплиц доступно только администратору");
        }
        if (!userHasRole(ctx.user(), "ROLE_AGRONOMIST")) {
            throw new AccessDeniedException("Недостаточно прав");
        }
    }

    public void assertAdminOnly() {
        if (!currentContext().admin()) {
            throw new AccessDeniedException("Только администратор");
        }
    }

    public void assertReadableZone(long zoneId) {
        long ghId = greenhouseIdOfZone(zoneId);
        assertReadableGreenhouse(ghId);
    }

    public void assertManageableZone(long zoneId) {
        long ghId = greenhouseIdOfZone(zoneId);
        assertManageableGreenhouse(ghId);
    }

    public void assertReadableSensor(long sensorId) {
        long ghId = greenhouseIdOfSensor(sensorId);
        assertReadableGreenhouse(ghId);
    }

    public void assertManageableSensor(long sensorId) {
        long ghId = greenhouseIdOfSensor(sensorId);
        assertManageableGreenhouse(ghId);
    }

    @Transactional(readOnly = true)
    public long greenhouseIdOfZone(long zoneId) {
        Zone z = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new NotFoundException("Секция не найдена: " + zoneId));
        return z.getGreenhouse().getId();
    }

    @Transactional(readOnly = true)
    public long greenhouseIdOfSensor(long sensorId) {
        Sensor s = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new NotFoundException("Датчик не найден: " + sensorId));
        return s.getZone().getGreenhouse().getId();
    }

    private static boolean userHasRole(CustomUserDetails user, String role) {
        return user.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(role::equals);
    }

    public record AccessContext(
            CustomUserDetails user,
            boolean admin,
            boolean scoped,
            Map<Long, AccessLevel> levelsByGreenhouseId
    ) {
        public boolean canReadGreenhouse(long greenhouseId) {
            if (admin || !scoped) {
                return true;
            }
            return levelsByGreenhouseId.containsKey(greenhouseId);
        }

        public boolean canManageGreenhouse(long greenhouseId) {
            if (admin) {
                return true;
            }
            if (!scoped) {
                return userHasRole(user, "ROLE_AGRONOMIST");
            }
            return levelsByGreenhouseId.get(greenhouseId) == AccessLevel.MANAGE
                    && userHasRole(user, "ROLE_AGRONOMIST");
        }
    }
}
