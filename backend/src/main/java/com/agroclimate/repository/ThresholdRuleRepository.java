package com.agroclimate.repository;

import com.agroclimate.domain.entity.ThresholdRule;
import com.agroclimate.domain.enums.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ThresholdRuleRepository extends JpaRepository<ThresholdRule, Long> {

    @Query("SELECT r FROM ThresholdRule r JOIN FETCH r.zone z JOIN FETCH z.greenhouse")
    List<ThresholdRule> findAllFetched();

    List<ThresholdRule> findByZoneId(Long zoneId);
    List<ThresholdRule> findByZoneIdAndMetricAndEnabledTrue(Long zoneId, Metric metric);
}
