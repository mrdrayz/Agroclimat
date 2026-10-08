package com.agroclimate.repository;

import com.agroclimate.domain.entity.GreenhouseUser;
import com.agroclimate.domain.entity.GreenhouseUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GreenhouseUserRepository
        extends JpaRepository<GreenhouseUser, GreenhouseUserId> {

    @Query("SELECT gu FROM GreenhouseUser gu JOIN FETCH gu.greenhouse JOIN FETCH gu.user WHERE gu.id.userId = :userId")
    List<GreenhouseUser> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT gu FROM GreenhouseUser gu JOIN FETCH gu.greenhouse JOIN FETCH gu.user WHERE gu.id.greenhouseId = :greenhouseId")
    List<GreenhouseUser> findAllByGreenhouseId(@Param("greenhouseId") Long greenhouseId);

    @Query("SELECT gu FROM GreenhouseUser gu JOIN FETCH gu.greenhouse JOIN FETCH gu.user WHERE gu.id.userId = :userId AND gu.id.greenhouseId = :greenhouseId")
    Optional<GreenhouseUser> findOne(@Param("userId") Long userId, @Param("greenhouseId") Long greenhouseId);

    @Query("SELECT gu FROM GreenhouseUser gu JOIN FETCH gu.greenhouse JOIN FETCH gu.user")
    List<GreenhouseUser> findAllWithDetails();
}
