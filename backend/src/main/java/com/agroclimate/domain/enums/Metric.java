package com.agroclimate.domain.enums;

public enum Metric {
    TEMPERATURE("°C"),
    HUMIDITY("%"),
    LIGHT("lx");

    private final String unit;

    Metric(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }
}
