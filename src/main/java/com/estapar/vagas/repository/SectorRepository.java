package com.estapar.vagas.repository;

import com.estapar.vagas.domain.entity.Sector;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SectorRepository extends JpaRepository<Sector, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Sector s WHERE s.name = :name")
    Optional<Sector> findByNameForUpdate(@Param("name") String name);

    @Query("SELECT COALESCE(SUM(s.maxCapacity), 0) FROM Sector s")
    int sumTotalCapacity();

    @Query("SELECT COALESCE(SUM(s.currentOccupancy), 0) FROM Sector s")
    int sumTotalOccupancy();
}
