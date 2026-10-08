package com.agroclimate.controller;

import com.agroclimate.dto.MeasurementDto;
import com.agroclimate.dto.SensorDto;
import com.agroclimate.dto.SensorRequest;
import com.agroclimate.service.MeasurementService;
import com.agroclimate.service.SensorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService service;
    private final MeasurementService measurementService;

    @GetMapping
    public List<SensorDto> findAll(@RequestParam(required = false) Long zoneId) {
        return zoneId == null ? service.findAll() : service.findByZone(zoneId);
    }

    @GetMapping("/{id}")
    public SensorDto findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public SensorDto create(@Valid @RequestBody SensorRequest req) { return service.create(req); }

    @PutMapping("/{id}")
    public SensorDto update(@PathVariable Long id, @Valid @RequestBody SensorRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }

    /** Измерения конкретного датчика. Каждое DTO содержит value и unit. */
    @GetMapping("/{id}/measurements")
    public List<MeasurementDto> measurements(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        if (from == null && to == null) {
            return measurementService.findBySensor(id);
        }
        return measurementService.findBySensorRange(id, from, to);
    }
}
