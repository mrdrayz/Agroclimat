package com.agroclimate.controller;

import com.agroclimate.dto.AlertDto;
import com.agroclimate.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService service;

    @GetMapping
    public List<AlertDto> findAll(
            @RequestParam(required = false) Boolean openOnly,
            @RequestParam(required = false) Long sensorId
    ) {
        if (sensorId != null) return service.findBySensor(sensorId);
        if (Boolean.TRUE.equals(openOnly)) return service.findOpen();
        return service.findAll();
    }

    @PostMapping("/{id}/acknowledge")
    public AlertDto acknowledge(@PathVariable Long id) { return service.acknowledge(id); }

    @PostMapping("/{id}/close")
    public AlertDto close(@PathVariable Long id) { return service.close(id); }
}
