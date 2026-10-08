package com.agroclimate.service;

import com.agroclimate.domain.entity.Greenhouse;
import com.agroclimate.dto.GreenhouseDto;
import com.agroclimate.dto.GreenhouseRequest;
import com.agroclimate.exception.NotFoundException;
import com.agroclimate.repository.GreenhouseRepository;
import com.agroclimate.service.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GreenhouseService {

    private final GreenhouseRepository repository;
    private final EntityMapper mapper;
    private final GreenhouseAccessService greenhouseAccessService;

    public List<GreenhouseDto> findAll() {
        var ctx = greenhouseAccessService.currentContext();
        return repository.findAll().stream()
                .filter(g -> ctx.canReadGreenhouse(g.getId()))
                .map(mapper::toGreenhouseDto)
                .toList();
    }

    public GreenhouseDto findById(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Greenhouse not found: " + id);
        }
        greenhouseAccessService.assertReadableGreenhouse(id);
        return mapper.toGreenhouseDto(getOrThrow(id));
    }

    @Transactional
    public GreenhouseDto create(GreenhouseRequest req) {
        greenhouseAccessService.assertMayCreateGreenhouse();
        Greenhouse g = Greenhouse.builder()
                .name(req.name())
                .location(req.location())
                .build();
        return mapper.toGreenhouseDto(repository.save(g));
    }

    @Transactional
    public GreenhouseDto update(Long id, GreenhouseRequest req) {
        greenhouseAccessService.assertManageableGreenhouse(id);
        Greenhouse g = getOrThrow(id);
        g.setName(req.name());
        g.setLocation(req.location());
        return mapper.toGreenhouseDto(g);
    }

    @Transactional
    public void delete(Long id) {
        greenhouseAccessService.assertAdminOnly();
        if (!repository.existsById(id)) {
            throw new NotFoundException("Greenhouse not found: " + id);
        }
        repository.deleteById(id);
    }

    public Greenhouse getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Greenhouse not found: " + id));
    }
}
