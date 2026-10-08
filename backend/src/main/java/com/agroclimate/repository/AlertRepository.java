package com.agroclimate.repository;

import com.agroclimate.domain.entity.Alert;
import com.agroclimate.domain.enums.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    @Query("SELECT DISTINCT a FROM Alert a JOIN FETCH a.sensor s JOIN FETCH s.zone z JOIN FETCH z.greenhouse JOIN FETCH a.rule")
    List<Alert> findAllFetched();

    @Query("SELECT DISTINCT a FROM Alert a JOIN FETCH a.sensor s JOIN FETCH s.zone z JOIN FETCH z.greenhouse JOIN FETCH a.rule WHERE a.status = :st")
    List<Alert> findByStatusFetched(@Param("st") AlertStatus status);

    @Query("SELECT a FROM Alert a JOIN FETCH a.sensor s JOIN FETCH s.zone z JOIN FETCH z.greenhouse JOIN FETCH a.rule WHERE s.id = :sensorId ORDER BY a.startedAt DESC")
    List<Alert> findBySensorIdFetched(@Param("sensorId") Long sensorId);

    @Query("SELECT a FROM Alert a JOIN FETCH a.sensor s JOIN FETCH s.zone z JOIN FETCH z.greenhouse JOIN FETCH a.rule WHERE a.id = :id")
    Optional<Alert> findByIdFetched(@Param("id") Long id);

    List<Alert> findByStatus(AlertStatus status);

    List<Alert> findBySensorIdOrderByStartedAtDesc(Long sensorId);

    Optional<Alert> findFirstByRuleIdAndSensorIdAndStatus(
            Long ruleId, Long sensorId, AlertStatus status);
}
