package com.agroclimate.repository;

import com.agroclimate.domain.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ZoneRepository extends JpaRepository<Zone, Long> {

    @Query("SELECT z FROM Zone z JOIN FETCH z.greenhouse gh WHERE gh.id = :greenhouseId")
    List<Zone> findByGreenhouseId(@Param("greenhouseId") Long greenhouseId);

    @Query("SELECT z FROM Zone z JOIN FETCH z.greenhouse")
    List<Zone> findAllFetched();
}
