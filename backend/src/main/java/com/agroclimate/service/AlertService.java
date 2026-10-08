package com.agroclimate.service;

import com.agroclimate.domain.entity.Alert;
import com.agroclimate.domain.entity.Measurement;
import com.agroclimate.domain.entity.Sensor;
import com.agroclimate.domain.entity.ThresholdRule;
import com.agroclimate.domain.enums.AlertStatus;
import com.agroclimate.dto.AlertDto;
import com.agroclimate.exception.NotFoundException;
import com.agroclimate.repository.AlertRepository;
import com.agroclimate.repository.ThresholdRuleRepository;
import com.agroclimate.service.mapper.EntityMapper;
import com.agroclimate.util.AlertMessageRu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final ThresholdRuleRepository ruleRepository;
    private final EntityMapper mapper;
    private final GreenhouseAccessService greenhouseAccessService;

    @Transactional(readOnly = true)
    public List<AlertDto> findAll() {
        var ctx = greenhouseAccessService.currentContext();
        return alertRepository.findAllFetched().stream()
                .filter(a -> ctx.canReadGreenhouse(a.getSensor().getZone().getGreenhouse().getId()))
                .sorted(RECENT_FIRST)
                .map(mapper::toAlertDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlertDto> findOpen() {
        var ctx = greenhouseAccessService.currentContext();
        return alertRepository.findByStatusFetched(AlertStatus.OPEN)
                .stream()
                .filter(a -> ctx.canReadGreenhouse(a.getSensor().getZone().getGreenhouse().getId()))
                .sorted(RECENT_FIRST)
                .map(mapper::toAlertDto)
                .toList();
    }

    /** Сначала недавно завершённые / с меткой времени окончания, затем по времени начала. */
    private static final Comparator<Alert> RECENT_FIRST = Comparator
            .comparing(AlertService::activityTime, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(Alert::getId, Comparator.reverseOrder());

    private static LocalDateTime activityTime(Alert a) {
        if (a.getEndedAt() != null) {
            return a.getEndedAt();
        }
        return a.getStartedAt();
    }

    @Transactional(readOnly = true)
    public List<AlertDto> findBySensor(Long sensorId) {
        greenhouseAccessService.assertReadableSensor(sensorId);
        return alertRepository.findBySensorIdFetched(sensorId)
                .stream()
                .sorted(RECENT_FIRST)
                .map(mapper::toAlertDto)
                .toList();
    }

    @Transactional
    public AlertDto acknowledge(Long id) {
        Alert a = getOrThrow(id);
        greenhouseAccessService.assertReadableGreenhouse(
                a.getSensor().getZone().getGreenhouse().getId());
        a.setStatus(AlertStatus.ACK);
        // Время реакции: для сортировки «свежее сверху» и отображения в списке
        a.setEndedAt(LocalDateTime.now());
        return mapper.toAlertDto(a);
    }

    @Transactional
    public AlertDto close(Long id) {
        Alert a = getOrThrow(id);
        greenhouseAccessService.assertReadableGreenhouse(
                a.getSensor().getZone().getGreenhouse().getId());
        a.setStatus(AlertStatus.CLOSED);
        a.setEndedAt(LocalDateTime.now());
        return mapper.toAlertDto(a);
    }

    /**
     * Проверяет измерение по правилам зоны и создаёт/закрывает алерты.
     */
    @Transactional
    public void evaluate(Measurement m) {
        Sensor sensor = m.getSensor();
        Long zoneId = sensor.getZone().getId();
        BigDecimal value = m.getValue();

        List<ThresholdRule> rules = ruleRepository
                .findByZoneIdAndMetricAndEnabledTrue(zoneId, sensor.getMetric());

        for (ThresholdRule rule : rules) {
            boolean violated = isViolated(rule, value);
            var existingOpt = alertRepository.findFirstByRuleIdAndSensorIdAndStatus(
                    rule.getId(), sensor.getId(), AlertStatus.OPEN);

            if (violated) {
                if (existingOpt.isPresent()) {
                    Alert existing = existingOpt.get();
                    existing.setLastValue(value);
                } else {
                    Alert alert = Alert.builder()
                            .rule(rule)
                            .sensor(sensor)
                            .startedAt(m.getTs())
                            .status(AlertStatus.OPEN)
                            .lastValue(value)
                            .message(AlertMessageRu.format(sensor, rule, value))
                            .build();
                    alertRepository.save(alert);
                    log.info("Alert created: sensor={}, rule={}, value={}", sensor.getId(), rule.getId(), value);
                }
            } else if (existingOpt.isPresent()) {
                Alert existing = existingOpt.get();
                existing.setStatus(AlertStatus.CLOSED);
                existing.setEndedAt(m.getTs());
                existing.setLastValue(value);
                log.info("Alert auto-closed: id={}", existing.getId());
            }
        }
    }

    private boolean isViolated(ThresholdRule rule, BigDecimal value) {
        if (rule.getMinValue() != null && value.compareTo(rule.getMinValue()) < 0) return true;
        if (rule.getMaxValue() != null && value.compareTo(rule.getMaxValue()) > 0) return true;
        return false;
    }

    private Alert getOrThrow(Long id) {
        return alertRepository.findByIdFetched(id)
                .orElseThrow(() -> new NotFoundException("Alert not found: " + id));
    }
}
