package com.agroclimate.repository;

import com.agroclimate.domain.entity.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    Optional<Measurement> findFirstBySensor_IdOrderByTsDesc(Long sensorId);

    List<Measurement> findBySensorIdOrderByTsDesc(Long sensorId);

    List<Measurement> findBySensorIdAndTsBetweenOrderByTsAsc(
            Long sensorId, LocalDateTime from, LocalDateTime to);
}
