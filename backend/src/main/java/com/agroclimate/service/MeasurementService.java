package com.agroclimate.service;

import com.agroclimate.domain.entity.Measurement;
import com.agroclimate.domain.entity.Sensor;
import com.agroclimate.dto.MeasurementDto;
import com.agroclimate.repository.MeasurementRepository;
import com.agroclimate.repository.SensorRepository;
import com.agroclimate.service.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MeasurementService {

    private final MeasurementRepository repository;
    private final SensorRepository sensorRepository;
    private final EntityMapper mapper;
    private final GreenhouseAccessService greenhouseAccessService;

    public List<MeasurementDto> findBySensor(Long sensorId) {
        greenhouseAccessService.assertReadableSensor(sensorId);
        return repository.findBySensorIdOrderByTsDesc(sensorId)
                .stream().map(mapper::toMeasurementDto).toList();
    }

    public List<MeasurementDto> findBySensorRange(Long sensorId, LocalDateTime from, LocalDateTime to) {
        greenhouseAccessService.assertReadableSensor(sensorId);
        if (from == null) from = LocalDateTime.now().minusDays(1);
        if (to == null) to = LocalDateTime.now();
        return repository.findBySensorIdAndTsBetweenOrderByTsAsc(sensorId, from, to)
                .stream().map(mapper::toMeasurementDto).toList();
    }

    public MeasurementDto save(Measurement m) {
        return mapper.toMeasurementDto(repository.save(m));
    }

    /** Последнее измерение по каждому датчику (для живого дашборда). */
    public List<MeasurementDto> findLatestPerSensor() {
        var ctx = greenhouseAccessService.currentContext();
        return sensorRepository.findAllFetched().stream()
                .filter(s -> ctx.canReadGreenhouse(s.getZone().getGreenhouse().getId()))
                .map(Sensor::getId)
                .map(repository::findFirstBySensor_IdOrderByTsDesc)
                .flatMap(Optional::stream)
                .map(mapper::toMeasurementDto)
                .toList();
    }
}
