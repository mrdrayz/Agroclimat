package com.agroclimate.service;

import com.agroclimate.domain.entity.Sensor;
import com.agroclimate.domain.enums.SensorStatus;
import com.agroclimate.dto.SensorDto;
import com.agroclimate.dto.SensorRequest;
import com.agroclimate.exception.NotFoundException;
import com.agroclimate.repository.SensorRepository;
import com.agroclimate.service.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorRepository repository;
    private final ZoneService zoneService;
    private final EntityMapper mapper;
    private final GreenhouseAccessService greenhouseAccessService;

    public List<SensorDto> findAll() {
        var ctx = greenhouseAccessService.currentContext();
        return repository.findAllFetched().stream()
                .filter(s -> ctx.canReadGreenhouse(s.getZone().getGreenhouse().getId()))
                .map(mapper::toSensorDto)
                .toList();
    }

    public List<SensorDto> findByZone(Long zoneId) {
        greenhouseAccessService.assertReadableZone(zoneId);
        return repository.findByZoneId(zoneId).stream().map(mapper::toSensorDto).toList();
    }

    public SensorDto findById(Long id) {
        Sensor s = getOrThrow(id);
        greenhouseAccessService.assertReadableSensor(s.getId());
        return mapper.toSensorDto(s);
    }

    @Transactional
    public SensorDto create(SensorRequest req) {
        greenhouseAccessService.assertManageableZone(req.zoneId());
        Sensor s = Sensor.builder()
                .zone(zoneService.getOrThrow(req.zoneId()))
                .metric(req.metric())
                .name(req.name())
                .status(req.status() == null ? SensorStatus.ACTIVE : req.status())
                .build();
        return mapper.toSensorDto(repository.save(s));
    }

    @Transactional
    public SensorDto update(Long id, SensorRequest req) {
        Sensor s = getOrThrow(id);
        greenhouseAccessService.assertManageableSensor(s.getId());
        greenhouseAccessService.assertManageableZone(req.zoneId());
        s.setZone(zoneService.getOrThrow(req.zoneId()));
        s.setMetric(req.metric());
        s.setName(req.name());
        if (req.status() != null) s.setStatus(req.status());
        return mapper.toSensorDto(s);
    }

    @Transactional
    public void delete(Long id) {
        Sensor s = getOrThrow(id);
        greenhouseAccessService.assertManageableSensor(s.getId());
        repository.deleteById(id);
    }

    public Sensor getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sensor not found: " + id));
    }
}
