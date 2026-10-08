package com.agroclimate.service;

import com.agroclimate.domain.entity.Zone;
import com.agroclimate.dto.ZoneDto;
import com.agroclimate.dto.ZoneRequest;
import com.agroclimate.exception.NotFoundException;
import com.agroclimate.repository.ZoneRepository;
import com.agroclimate.service.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository repository;
    private final GreenhouseService greenhouseService;
    private final EntityMapper mapper;
    private final GreenhouseAccessService greenhouseAccessService;

    public List<ZoneDto> findAll() {
        var ctx = greenhouseAccessService.currentContext();
        return repository.findAllFetched().stream()
                .filter(z -> ctx.canReadGreenhouse(z.getGreenhouse().getId()))
                .map(mapper::toZoneDto)
                .toList();
    }

    public List<ZoneDto> findByGreenhouse(Long greenhouseId) {
        greenhouseAccessService.assertReadableGreenhouse(greenhouseId);
        return repository.findByGreenhouseId(greenhouseId).stream().map(mapper::toZoneDto).toList();
    }

    public ZoneDto findById(Long id) {
        Zone z = getOrThrow(id);
        greenhouseAccessService.assertReadableGreenhouse(z.getGreenhouse().getId());
        return mapper.toZoneDto(z);
    }

    @Transactional
    public ZoneDto create(ZoneRequest req) {
        greenhouseAccessService.assertManageableGreenhouse(req.greenhouseId());
        Zone z = Zone.builder()
                .greenhouse(greenhouseService.getOrThrow(req.greenhouseId()))
                .name(req.name())
                .description(req.description())
                .areaM2(req.areaM2())
                .build();
        return mapper.toZoneDto(repository.save(z));
    }

    @Transactional
    public ZoneDto update(Long id, ZoneRequest req) {
        Zone z = getOrThrow(id);
        greenhouseAccessService.assertManageableGreenhouse(z.getGreenhouse().getId());
        greenhouseAccessService.assertManageableGreenhouse(req.greenhouseId());
        z.setGreenhouse(greenhouseService.getOrThrow(req.greenhouseId()));
        z.setName(req.name());
        z.setDescription(req.description());
        z.setAreaM2(req.areaM2());
        return mapper.toZoneDto(z);
    }

    @Transactional
    public void delete(Long id) {
        Zone z = getOrThrow(id);
        greenhouseAccessService.assertManageableGreenhouse(z.getGreenhouse().getId());
        repository.deleteById(id);
    }

    public Zone getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Zone not found: " + id));
    }
}
