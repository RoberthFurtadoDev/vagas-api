package com.estapar.vagas.repository;

import com.estapar.vagas.domain.entity.Spot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpotRepository extends JpaRepository<Spot, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sp FROM Spot sp WHERE sp.lat = :lat AND sp.lng = :lng")
    Optional<Spot> findByLatAndLngForUpdate(@Param("lat") double lat, @Param("lng") double lng);
}
