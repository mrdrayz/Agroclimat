package com.agroclimate.domain.entity;

import com.agroclimate.domain.enums.RoleCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleCode code;

    private String name;

    @Column(columnDefinition = "text")
    private String description;
}
