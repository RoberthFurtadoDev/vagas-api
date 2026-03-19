package com.estapar.vagas.repository;

import com.estapar.vagas.domain.entity.ParkingSession;
import com.estapar.vagas.domain.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Repository
public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {

    Optional<ParkingSession> findByLicensePlateAndStatus(String licensePlate, SessionStatus status);

    boolean existsByLicensePlateAndStatusNot(String licensePlate, SessionStatus status);

    @Query("""
            SELECT COALESCE(SUM(ps.amount), 0)
            FROM ParkingSession ps
            WHERE ps.sectorName = :sector
              AND ps.status = 'EXITED'
              AND ps.exitTime >= :startOfDay
              AND ps.exitTime < :endOfDay
            """)
    BigDecimal sumAmountBySectorAndDate(
            @Param("sector") String sector,
            @Param("startOfDay") Instant startOfDay,
            @Param("endOfDay") Instant endOfDay
    );
}
