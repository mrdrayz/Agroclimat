package com.agroclimate.service;

import com.agroclimate.domain.entity.ThresholdRule;
import com.agroclimate.dto.ThresholdRuleDto;
import com.agroclimate.dto.ThresholdRuleRequest;
import com.agroclimate.exception.NotFoundException;
import com.agroclimate.repository.ThresholdRuleRepository;
import com.agroclimate.service.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ThresholdRuleService {

    private final ThresholdRuleRepository repository;
    private final ZoneService zoneService;
    private final EntityMapper mapper;
    private final GreenhouseAccessService greenhouseAccessService;

    public List<ThresholdRuleDto> findAll() {
        var ctx = greenhouseAccessService.currentContext();
        return repository.findAllFetched().stream()
                .filter(r -> ctx.canReadGreenhouse(r.getZone().getGreenhouse().getId()))
                .map(mapper::toRuleDto)
                .toList();
    }

    public List<ThresholdRuleDto> findByZone(Long zoneId) {
        greenhouseAccessService.assertReadableZone(zoneId);
        return repository.findByZoneId(zoneId).stream().map(mapper::toRuleDto).toList();
    }

    public ThresholdRuleDto findById(Long id) {
        ThresholdRule r = getOrThrow(id);
        greenhouseAccessService.assertReadableZone(r.getZone().getId());
        return mapper.toRuleDto(r);
    }

    @Transactional
    public ThresholdRuleDto create(ThresholdRuleRequest req) {
        greenhouseAccessService.assertManageableZone(req.zoneId());
        ThresholdRule r = ThresholdRule.builder()
                .zone(zoneService.getOrThrow(req.zoneId()))
                .metric(req.metric())
                .minValue(req.minValue())
                .maxValue(req.maxValue())
                .enabled(req.enabled() == null ? Boolean.TRUE : req.enabled())
                .severity(req.severity())
                .build();
        return mapper.toRuleDto(repository.save(r));
    }

    @Transactional
    public ThresholdRuleDto update(Long id, ThresholdRuleRequest req) {
        ThresholdRule r = getOrThrow(id);
        greenhouseAccessService.assertManageableZone(r.getZone().getId());
        greenhouseAccessService.assertManageableZone(req.zoneId());
        r.setZone(zoneService.getOrThrow(req.zoneId()));
        r.setMetric(req.metric());
        r.setMinValue(req.minValue());
        r.setMaxValue(req.maxValue());
        if (req.enabled() != null) r.setEnabled(req.enabled());
        r.setSeverity(req.severity());
        return mapper.toRuleDto(r);
    }

    @Transactional
    public void delete(Long id) {
        ThresholdRule r = getOrThrow(id);
        greenhouseAccessService.assertManageableZone(r.getZone().getId());
        repository.deleteById(id);
    }

    public ThresholdRule getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rule not found: " + id));
    }
}
