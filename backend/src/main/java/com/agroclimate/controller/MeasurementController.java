package com.agroclimate.controller;

import com.agroclimate.dto.MeasurementDto;
import com.agroclimate.service.MeasurementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MeasurementController {

    private final MeasurementService measurementService;

    /** Последнее измерение по каждому датчику (удобно для опроса с фронтенда). */
    @GetMapping("/latest")
    public List<MeasurementDto> latestPerSensor() {
        return measurementService.findLatestPerSensor();
    }
}
