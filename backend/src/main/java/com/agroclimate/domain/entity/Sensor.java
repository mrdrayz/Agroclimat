package com.agroclimate.domain.entity;

import com.agroclimate.domain.enums.Metric;
import com.agroclimate.domain.enums.SensorStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Metric metric;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SensorStatus status = SensorStatus.ACTIVE;

    @Column(name = "installed_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime installedAt = LocalDateTime.now();
}
