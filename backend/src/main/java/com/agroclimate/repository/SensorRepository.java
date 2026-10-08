package com.agroclimate.repository;

import com.agroclimate.domain.entity.Sensor;
import com.agroclimate.domain.enums.SensorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SensorRepository extends JpaRepository<Sensor, Long> {

    @Query("SELECT s FROM Sensor s JOIN FETCH s.zone z JOIN FETCH z.greenhouse WHERE z.id = :zoneId")
    List<Sensor> findByZoneId(@Param("zoneId") Long zoneId);

    @Query("SELECT s FROM Sensor s JOIN FETCH s.zone z JOIN FETCH z.greenhouse")
    List<Sensor> findAllFetched();

    List<Sensor> findByStatus(SensorStatus status);
}
