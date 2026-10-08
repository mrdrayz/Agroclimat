package com.agroclimate.controller;

import com.agroclimate.dto.ZoneDto;
import com.agroclimate.dto.ZoneRequest;
import com.agroclimate.service.ZoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService service;

    @GetMapping
    public List<ZoneDto> findAll(@RequestParam(required = false) Long greenhouseId) {
        return greenhouseId == null ? service.findAll() : service.findByGreenhouse(greenhouseId);
    }

    @GetMapping("/{id}")
    public ZoneDto findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public ZoneDto create(@Valid @RequestBody ZoneRequest req) { return service.create(req); }

    @PutMapping("/{id}")
    public ZoneDto update(@PathVariable Long id, @Valid @RequestBody ZoneRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
