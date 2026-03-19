package com.estapar.vagas.service;

import com.estapar.vagas.domain.entity.ParkingSession;
import com.estapar.vagas.domain.entity.Sector;
import com.estapar.vagas.domain.entity.Spot;
import com.estapar.vagas.domain.enums.SessionStatus;
import com.estapar.vagas.domain.exception.ActiveSessionAlreadyExistsException;
import com.estapar.vagas.domain.exception.GarageFullException;
import com.estapar.vagas.domain.exception.SpotNotFoundException;
import com.estapar.vagas.domain.exception.VehicleNotFoundException;
import com.estapar.vagas.dto.request.WebhookRequest;
import com.estapar.vagas.repository.ParkingSessionRepository;
import com.estapar.vagas.repository.SectorRepository;
import com.estapar.vagas.repository.SpotRepository;
import com.estapar.vagas.service.pricing.PricingStrategy;
import com.estapar.vagas.service.pricing.PricingStrategyResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private static final long FREE_PERIOD_MINUTES = 30;

    private final ParkingSessionRepository sessionRepository;
    private final SectorRepository sectorRepository;
    private final SpotRepository spotRepository;
    private final PricingStrategyResolver pricingStrategyResolver;

    @Transactional
    public void handleEntry(WebhookRequest request) {
        validateNoActiveSession(request.licensePlate());
        validateGarageCapacity();

        final int totalOccupied = sectorRepository.sumTotalOccupancy();
        final int totalCapacity = sectorRepository.sumTotalCapacity();
        final PricingStrategy strategy = pricingStrategyResolver.resolve(totalOccupied, totalCapacity);

        final ParkingSession session = new ParkingSession(
                request.licensePlate(),
                request.entryTime(),
                strategy.modifier()
        );

        sessionRepository.save(session);
        log.info("ENTRY recorded for plate={}, modifier={}", request.licensePlate(), strategy.modifier());
    }

    @Transactional
    public void handleParked(WebhookRequest request) {
        final ParkingSession session = findActiveSession(request.licensePlate(), SessionStatus.ENTERED);
        final Spot spot = findSpotForUpdate(request.lat(), request.lng());
        final Sector sector = sectorRepository.findByNameForUpdate(spot.getSector().getName())
                .orElseThrow(() -> new IllegalStateException("Sector not found: " + spot.getSector().getName()));

        spot.setOccupied(true);
        sector.incrementOccupancy();

        final BigDecimal pricePerHour = sector.getBasePrice()
                .multiply(session.getOccupancyModifier())
                .setScale(2, RoundingMode.HALF_UP);

        session.setSpot(spot);
        session.setSectorName(sector.getName());
        session.setPricePerHour(pricePerHour);
        session.setStatus(SessionStatus.PARKED);

        spotRepository.save(spot);
        sectorRepository.save(sector);
        sessionRepository.save(session);

        log.info("PARKED recorded for plate={}, sector={}, pricePerHour={}",
                request.licensePlate(), sector.getName(), pricePerHour);
    }

    @Transactional
    public void handleExit(WebhookRequest request) {
        final ParkingSession session = findActiveSession(request.licensePlate(), SessionStatus.PARKED);
        final Spot spot = session.getSpot();
        final Sector sector = sectorRepository.findByNameForUpdate(session.getSectorName())
                .orElseThrow(() -> new IllegalStateException("Sector not found: " + session.getSectorName()));

        final BigDecimal amount = calculateAmount(session.getEntryTime(), request.exitTime(), session.getPricePerHour());

        spot.setOccupied(false);
        sector.decrementOccupancy();

        session.setExitTime(request.exitTime());
        session.setAmount(amount);
        session.setStatus(SessionStatus.EXITED);

        spotRepository.save(spot);
        sectorRepository.save(sector);
        sessionRepository.save(session);

        log.info("EXIT recorded for plate={}, amount={}", request.licensePlate(), amount);
    }

    private void validateNoActiveSession(String licensePlate) {
        final boolean hasActive = sessionRepository.existsByLicensePlateAndStatusNot(
                licensePlate, SessionStatus.EXITED);
        if (hasActive) {
            throw new ActiveSessionAlreadyExistsException(licensePlate);
        }
    }

    private void validateGarageCapacity() {
        final int totalOccupied = sectorRepository.sumTotalOccupancy();
        final int totalCapacity = sectorRepository.sumTotalCapacity();
        if (totalCapacity > 0 && totalOccupied >= totalCapacity) {
            throw new GarageFullException();
        }
    }

    private ParkingSession findActiveSession(String licensePlate, SessionStatus status) {
        return sessionRepository.findByLicensePlateAndStatus(licensePlate, status)
                .orElseThrow(() -> new VehicleNotFoundException(licensePlate));
    }

    private Spot findSpotForUpdate(double lat, double lng) {
        return spotRepository.findByLatAndLngForUpdate(lat, lng)
                .orElseThrow(() -> new SpotNotFoundException(lat, lng));
    }

    private BigDecimal calculateAmount(Instant entryTime, Instant exitTime, BigDecimal pricePerHour) {
        final long totalMinutes = Duration.between(entryTime, exitTime).toMinutes();

        if (totalMinutes <= FREE_PERIOD_MINUTES) {
            return BigDecimal.ZERO;
        }

        final long hours = (long) Math.ceil(totalMinutes / 60.0);
        return pricePerHour.multiply(BigDecimal.valueOf(hours)).setScale(2, RoundingMode.HALF_UP);
    }
}
