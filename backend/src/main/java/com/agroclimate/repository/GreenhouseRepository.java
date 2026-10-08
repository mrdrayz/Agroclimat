package com.agroclimate.repository;

import com.agroclimate.domain.entity.Greenhouse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GreenhouseRepository extends JpaRepository<Greenhouse, Long> {
}
