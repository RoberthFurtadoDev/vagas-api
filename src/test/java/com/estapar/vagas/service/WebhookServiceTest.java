package com.estapar.vagas.service;

import com.estapar.vagas.domain.entity.ParkingSession;
import com.estapar.vagas.domain.entity.Sector;
import com.estapar.vagas.domain.entity.Spot;
import com.estapar.vagas.domain.enums.EventType;
import com.estapar.vagas.domain.enums.SessionStatus;
import com.estapar.vagas.domain.exception.GarageFullException;
import com.estapar.vagas.domain.exception.VehicleNotFoundException;
import com.estapar.vagas.dto.request.WebhookRequest;
import com.estapar.vagas.repository.ParkingSessionRepository;
import com.estapar.vagas.repository.SectorRepository;
import com.estapar.vagas.repository.SpotRepository;
import com.estapar.vagas.service.pricing.PricingStrategyResolver;
import com.estapar.vagas.service.pricing.StandardPricingStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private ParkingSessionRepository sessionRepository;

    @Mock
    private SectorRepository sectorRepository;

    @Mock
    private SpotRepository spotRepository;

    @Mock
    private PricingStrategyResolver pricingStrategyResolver;

    @InjectMocks
    private WebhookService webhookService;

    private static final String PLATE = "ZUL0001";

    @Test
    void handleEntry_shouldSaveSessionWithModifier() {
        when(sessionRepository.existsByLicensePlateAndStatusNot(PLATE, SessionStatus.EXITED)).thenReturn(false);
        when(sectorRepository.sumTotalOccupancy()).thenReturn(30);
        when(sectorRepository.sumTotalCapacity()).thenReturn(100);
        when(pricingStrategyResolver.resolve(30, 100)).thenReturn(new StandardPricingStrategy());

        final WebhookRequest request = new WebhookRequest(PLATE, Instant.now(), null, null, null, EventType.ENTRY);
        webhookService.handleEntry(request);

        verify(sessionRepository).save(any(ParkingSession.class));
    }

    @Test
    void handleEntry_shouldThrowWhenGarageIsFull() {
        when(sessionRepository.existsByLicensePlateAndStatusNot(PLATE, SessionStatus.EXITED)).thenReturn(false);
        when(sectorRepository.sumTotalOccupancy()).thenReturn(100);
        when(sectorRepository.sumTotalCapacity()).thenReturn(100);

        final WebhookRequest request = new WebhookRequest(PLATE, Instant.now(), null, null, null, EventType.ENTRY);
        assertThrows(GarageFullException.class, () -> webhookService.handleEntry(request));
    }

    @Test
    void handleParked_shouldSetSpotOccupiedAndPricePerHour() {
        final Sector sector = new Sector("A", new BigDecimal("10.00"), 100);
        sector.setCurrentOccupancy(30);
        final Spot spot = new Spot(1L, sector, -23.56, -46.65);
        final ParkingSession session = new ParkingSession(PLATE, Instant.now(), new BigDecimal("1.10"));

        when(sessionRepository.findByLicensePlateAndStatus(PLATE, SessionStatus.ENTERED))
                .thenReturn(Optional.of(session));
        when(spotRepository.findByLatAndLngForUpdate(-23.56, -46.65))
                .thenReturn(Optional.of(spot));
        when(sectorRepository.findByNameForUpdate("A"))
                .thenReturn(Optional.of(sector));

        final WebhookRequest request = new WebhookRequest(PLATE, null, null, -23.56, -46.65, EventType.PARKED);
        webhookService.handleParked(request);

        assertEquals(new BigDecimal("11.00"), session.getPricePerHour());
        assertEquals(SessionStatus.PARKED, session.getStatus());
    }

    @Test
    void handleExit_shouldCalculateAmountForStay() {
        final Sector sector = new Sector("A", new BigDecimal("10.00"), 100);
        final Spot spot = new Spot(1L, sector, -23.56, -46.65);
        final Instant entryTime = Instant.parse("2025-01-01T12:00:00Z");
        final Instant exitTime = Instant.parse("2025-01-01T13:31:00Z");

        final ParkingSession session = new ParkingSession(PLATE, entryTime, new BigDecimal("1.00"));
        session.setSpot(spot);
        session.setSectorName("A");
        session.setPricePerHour(new BigDecimal("10.00"));
        session.setStatus(SessionStatus.PARKED);

        when(sessionRepository.findByLicensePlateAndStatus(PLATE, SessionStatus.PARKED))
                .thenReturn(Optional.of(session));
        when(sectorRepository.findByNameForUpdate("A"))
                .thenReturn(Optional.of(sector));

        final WebhookRequest request = new WebhookRequest(PLATE, null, exitTime, null, null, EventType.EXIT);
        webhookService.handleExit(request);

        assertEquals(new BigDecimal("20.00"), session.getAmount());
        assertEquals(SessionStatus.EXITED, session.getStatus());
    }

    @Test
    void handleExit_shouldChargeZeroForFreePeriod() {
        final Sector sector = new Sector("A", new BigDecimal("10.00"), 100);
        final Spot spot = new Spot(1L, sector, -23.56, -46.65);
        final Instant entryTime = Instant.parse("2025-01-01T12:00:00Z");
        final Instant exitTime = Instant.parse("2025-01-01T12:30:00Z");

        final ParkingSession session = new ParkingSession(PLATE, entryTime, new BigDecimal("1.00"));
        session.setSpot(spot);
        session.setSectorName("A");
        session.setPricePerHour(new BigDecimal("10.00"));
        session.setStatus(SessionStatus.PARKED);

        when(sessionRepository.findByLicensePlateAndStatus(PLATE, SessionStatus.PARKED))
                .thenReturn(Optional.of(session));
        when(sectorRepository.findByNameForUpdate("A"))
                .thenReturn(Optional.of(sector));

        final WebhookRequest request = new WebhookRequest(PLATE, null, exitTime, null, null, EventType.EXIT);
        webhookService.handleExit(request);

        assertEquals(BigDecimal.ZERO, session.getAmount());
    }

    @Test
    void handleExit_shouldThrowWhenNoParkedSession() {
        when(sessionRepository.findByLicensePlateAndStatus(PLATE, SessionStatus.PARKED))
                .thenReturn(Optional.empty());

        final WebhookRequest request = new WebhookRequest(PLATE, null, Instant.now(), null, null, EventType.EXIT);
        assertThrows(VehicleNotFoundException.class, () -> webhookService.handleExit(request));
    }
}
