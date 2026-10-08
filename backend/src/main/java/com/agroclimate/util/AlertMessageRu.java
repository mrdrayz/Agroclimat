package com.agroclimate.util;

import com.agroclimate.domain.entity.Sensor;
import com.agroclimate.domain.entity.ThresholdRule;
import com.agroclimate.domain.enums.Metric;
import com.agroclimate.domain.enums.Severity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class AlertMessageRu {

    private AlertMessageRu() {}

    public static String format(Sensor sensor, ThresholdRule rule, BigDecimal lastValue) {
        return format(sensor.getMetric(), rule, lastValue);
    }

    public static String format(Metric metric, ThresholdRule rule, BigDecimal lastValue) {
        String metricName = metricDisplayNameRu(metric);
        String min = formatNumberRu(rule.getMinValue());
        String max = formatNumberRu(rule.getMaxValue());
        String unit = metric.getUnit();
        String val = formatNumberRu(lastValue);
        String sev = severityRu(rule.getSeverity());
        return String.format(
                "%s вне допустимого диапазона %s…%s %s (текущее значение %s %s). Важность: %s.",
                metricName,
                min,
                max,
                unit,
                val,
                unit,
                sev
        );
    }

    private static String metricDisplayNameRu(Metric m) {
        return switch (m) {
            case TEMPERATURE -> "Температура";
            case HUMIDITY -> "Влажность";
            case LIGHT -> "Освещённость";
        };
    }

    private static String severityRu(Severity s) {
        return switch (s) {
            case INFO -> "информация";
            case WARN -> "внимание";
            case CRITICAL -> "критично";
        };
    }

    private static String formatNumberRu(BigDecimal n) {
        if (n == null) {
            return "—";
        }
        DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance(Locale.forLanguageTag("ru-RU"));
        DecimalFormat df = new DecimalFormat("#0.##", sym);
        df.setMaximumFractionDigits(4);
        return df.format(n);
    }
}
