package com.agroclimate.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "greenhouse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Greenhouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String location;

    @OneToMany(mappedBy = "greenhouse", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Zone> zones = new ArrayList<>();
}
