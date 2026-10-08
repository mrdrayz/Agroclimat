package com.agroclimate.service;

import com.agroclimate.domain.entity.Measurement;
import com.agroclimate.domain.entity.Sensor;
import com.agroclimate.domain.enums.MeasurementSource;
import com.agroclimate.domain.enums.Metric;
import com.agroclimate.domain.enums.SensorStatus;
import com.agroclimate.repository.MeasurementRepository;
import com.agroclimate.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Симулятор данных с контроллеров.
 *
 * Раз в N миллисекунд (см. app.simulation.period-ms) проходит по всем
 * активным датчикам и генерирует случайное измерение. Значение слегка
 * "блуждает" вокруг предыдущего, чтобы график выглядел реалистично.
 *
 * После сохранения каждого измерения вызывается AlertService.evaluate(),
 * который сверяет значение с активными порогами и создаёт/закрывает алерты.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SensorSimulationService {

    private final SensorRepository sensorRepository;
    private final MeasurementRepository measurementRepository;
    private final AlertService alertService;

    @Value("${app.simulation.enabled:true}")
    private boolean enabled;

    /** Кэш предыдущего значения по каждому датчику для плавных колебаний. */
    private final ConcurrentHashMap<Long, BigDecimal> lastValues = new ConcurrentHashMap<>();

    @Scheduled(fixedDelayString = "${app.simulation.period-ms:5000}")
    @Transactional
    public void tick() {
        if (!enabled) return;

        List<Sensor> sensors = sensorRepository.findByStatus(SensorStatus.ACTIVE);
        if (sensors.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();

        for (Sensor sensor : sensors) {
            BigDecimal value = nextValue(sensor);
            Measurement m = Measurement.builder()
                    .sensor(sensor)
                    .ts(now)
                    .value(value)
                    .source(MeasurementSource.SIM)
                    .build();
            measurementRepository.save(m);
            alertService.evaluate(m);
        }

        log.debug("Simulated {} measurements at {}", sensors.size(), now);
    }

    private BigDecimal nextValue(Sensor sensor) {
        Metric metric = sensor.getMetric();
        BigDecimal previous = lastValues.get(sensor.getId());
        double prev = previous == null ? defaultValue(metric) : previous.doubleValue();

        // Случайное приращение в пределах [-step; +step]
        double step = stepFor(metric);
        double delta = ThreadLocalRandom.current().nextDouble(-step, step);
        double next = clamp(prev + delta, metric);

        BigDecimal result = BigDecimal.valueOf(next).setScale(2, RoundingMode.HALF_UP);
        lastValues.put(sensor.getId(), result);
        return result;
    }

    private double defaultValue(Metric metric) {
        return switch (metric) {
            case TEMPERATURE -> 22.0;     // °C
            case HUMIDITY    -> 60.0;     // %
            case LIGHT       -> 12000.0;  // lx
        };
    }

    private double stepFor(Metric metric) {
        return switch (metric) {
            case TEMPERATURE -> 0.6;
            case HUMIDITY    -> 1.5;
            case LIGHT       -> 600.0;
        };
    }

    /** Иногда (~3% случаев) намеренно генерируем выброс, чтобы сработали алерты. */
    private double clamp(double value, Metric metric) {
        double min, max;
        switch (metric) {
            case TEMPERATURE -> { min = -5;  max = 45;     }
            case HUMIDITY    -> { min = 10;  max = 100;    }
            case LIGHT       -> { min = 0;   max = 60_000; }
            default          -> { min = 0;   max = 1_000;  }
        }
        if (ThreadLocalRandom.current().nextInt(100) < 3) {
            value += (ThreadLocalRandom.current().nextBoolean() ? 1 : -1) * stepFor(metric) * 8;
        }
        return Math.max(min, Math.min(max, value));
    }
}
