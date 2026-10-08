package com.agroclimate.domain.entity;

import com.agroclimate.domain.enums.AccessLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "greenhouse_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GreenhouseUser {

    @EmbeddedId
    private GreenhouseUserId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("greenhouseId")
    @JoinColumn(name = "greenhouse_id")
    private Greenhouse greenhouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false)
    @Builder.Default
    private AccessLevel accessLevel = AccessLevel.VIEW;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();

    @PrePersist
    void prePersist() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }
}
