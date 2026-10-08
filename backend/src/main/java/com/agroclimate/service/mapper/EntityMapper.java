package com.agroclimate.service.mapper;

import com.agroclimate.domain.entity.*;
import com.agroclimate.dto.*;
import com.agroclimate.util.AlertMessageRu;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class EntityMapper {

    public UserDto toUserDto(AppUser user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getRoles().stream().map(r -> r.getCode().name()).collect(Collectors.toSet())
        );
    }

    public GreenhouseDto toGreenhouseDto(Greenhouse g) {
        return new GreenhouseDto(g.getId(), g.getName(), g.getLocation());
    }

    public ZoneDto toZoneDto(Zone z) {
        return new ZoneDto(
                z.getId(),
                z.getGreenhouse().getId(),
                z.getName(),
                z.getDescription(),
                z.getAreaM2()
        );
    }

    public SensorDto toSensorDto(Sensor s) {
        return new SensorDto(
                s.getId(),
                s.getZone().getId(),
                s.getMetric(),
                s.getMetric().getUnit(),
                s.getName(),
                s.getStatus(),
                s.getInstalledAt()
        );
    }

    public MeasurementDto toMeasurementDto(Measurement m) {
        return new MeasurementDto(
                m.getId(),
                m.getSensor().getId(),
                m.getSensor().getMetric(),
                m.getValue(),
                m.getSensor().getMetric().getUnit(),
                m.getTs(),
                m.getSource()
        );
    }

    public ThresholdRuleDto toRuleDto(ThresholdRule r) {
        return new ThresholdRuleDto(
                r.getId(),
                r.getZone().getId(),
                r.getMetric(),
                r.getMetric().getUnit(),
                r.getMinValue(),
                r.getMaxValue(),
                r.getEnabled(),
                r.getSeverity()
        );
    }

    public AlertDto toAlertDto(Alert a) {
        Sensor sensor = a.getSensor();
        ThresholdRule rule = a.getRule();
        Zone zone = sensor.getZone();
        Greenhouse gh = zone.getGreenhouse();
        return new AlertDto(
                a.getId(),
                rule.getId(),
                sensor.getId(),
                sensor.getMetric(),
                rule.getSeverity(),
                a.getStatus(),
                a.getStartedAt(),
                a.getEndedAt(),
                a.getLastValue(),
                sensor.getMetric().getUnit(),
                AlertMessageRu.format(sensor, rule, a.getLastValue()),
                gh.getId(),
                gh.getName(),
                zone.getId(),
                zone.getName()
        );
    }
}
