package com.agroclimate.controller;

import com.agroclimate.dto.GreenhouseDto;
import com.agroclimate.dto.GreenhouseRequest;
import com.agroclimate.service.GreenhouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/greenhouses")
@RequiredArgsConstructor
public class GreenhouseController {

    private final GreenhouseService service;

    @GetMapping
    public List<GreenhouseDto> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public GreenhouseDto findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public GreenhouseDto create(@Valid @RequestBody GreenhouseRequest req) { return service.create(req); }

    @PutMapping("/{id}")
    public GreenhouseDto update(@PathVariable Long id, @Valid @RequestBody GreenhouseRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
