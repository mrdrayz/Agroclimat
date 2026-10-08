package com.agroclimate.controller;

import com.agroclimate.dto.GreenhouseAssignmentDto;
import com.agroclimate.dto.UpsertGreenhouseAssignmentRequest;
import com.agroclimate.service.GreenhouseAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/greenhouse-assignments")
@RequiredArgsConstructor
public class GreenhouseAssignmentController {

    private final GreenhouseAssignmentService service;

    @GetMapping
    public List<GreenhouseAssignmentDto> list() {
        return service.findAll();
    }

    @PostMapping
    public GreenhouseAssignmentDto upsert(@Valid @RequestBody UpsertGreenhouseAssignmentRequest req) {
        return service.upsert(req);
    }

    @DeleteMapping
    public void remove(
            @RequestParam("greenhouseId") long greenhouseId,
            @RequestParam("userId") long userId
    ) {
        service.remove(greenhouseId, userId);
    }
}
