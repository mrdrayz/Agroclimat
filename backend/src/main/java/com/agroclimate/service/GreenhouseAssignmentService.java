package com.agroclimate.service;

import com.agroclimate.domain.entity.AppUser;
import com.agroclimate.domain.entity.Greenhouse;
import com.agroclimate.domain.entity.GreenhouseUser;
import com.agroclimate.domain.entity.GreenhouseUserId;
import com.agroclimate.dto.GreenhouseAssignmentDto;
import com.agroclimate.dto.UpsertGreenhouseAssignmentRequest;
import com.agroclimate.exception.NotFoundException;
import com.agroclimate.repository.AppUserRepository;
import com.agroclimate.repository.GreenhouseRepository;
import com.agroclimate.repository.GreenhouseUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GreenhouseAssignmentService {

    private final GreenhouseUserRepository greenhouseUserRepository;
    private final GreenhouseRepository greenhouseRepository;
    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public List<GreenhouseAssignmentDto> findAll() {
        return greenhouseUserRepository.findAllWithDetails().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public GreenhouseAssignmentDto upsert(UpsertGreenhouseAssignmentRequest req) {
        Greenhouse gh = greenhouseRepository.findById(req.greenhouseId())
                .orElseThrow(() -> new NotFoundException("Теплица не найдена: " + req.greenhouseId()));
        AppUser user = appUserRepository.findById(req.userId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + req.userId()));

        GreenhouseUserId id = new GreenhouseUserId(gh.getId(), user.getId());
        GreenhouseUser gu = greenhouseUserRepository.findOne(user.getId(), gh.getId())
                .orElseGet(() -> {
                    GreenhouseUser created = new GreenhouseUser();
                    created.setId(id);
                    created.setGreenhouse(gh);
                    created.setUser(user);
                    return created;
                });
        gu.setAccessLevel(req.accessLevel());
        return toDto(greenhouseUserRepository.save(gu));
    }

    @Transactional
    public void remove(long greenhouseId, long userId) {
        GreenhouseUserId id = new GreenhouseUserId(greenhouseId, userId);
        if (!greenhouseUserRepository.existsById(id)) {
            throw new NotFoundException("Назначение не найдено");
        }
        greenhouseUserRepository.deleteById(id);
    }

    private GreenhouseAssignmentDto toDto(GreenhouseUser gu) {
        return new GreenhouseAssignmentDto(
                gu.getGreenhouse().getId(),
                gu.getGreenhouse().getName(),
                gu.getUser().getId(),
                gu.getUser().getEmail(),
                gu.getAccessLevel().name(),
                gu.getAssignedAt()
        );
    }
}
