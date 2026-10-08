package com.agroclimate.controller;

import com.agroclimate.dto.ThresholdRuleDto;
import com.agroclimate.dto.ThresholdRuleRequest;
import com.agroclimate.service.ThresholdRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/threshold-rules")
@RequiredArgsConstructor
public class ThresholdRuleController {

    private final ThresholdRuleService service;

    @GetMapping
    public List<ThresholdRuleDto> findAll(@RequestParam(required = false) Long zoneId) {
        return zoneId == null ? service.findAll() : service.findByZone(zoneId);
    }

    @GetMapping("/{id}")
    public ThresholdRuleDto findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public ThresholdRuleDto create(@Valid @RequestBody ThresholdRuleRequest req) { return service.create(req); }

    @PutMapping("/{id}")
    public ThresholdRuleDto update(@PathVariable Long id, @Valid @RequestBody ThresholdRuleRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
