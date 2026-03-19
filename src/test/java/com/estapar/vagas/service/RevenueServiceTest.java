package com.estapar.vagas.service;

import com.estapar.vagas.dto.request.RevenueRequest;
import com.estapar.vagas.dto.response.RevenueResponse;
import com.estapar.vagas.repository.ParkingSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevenueServiceTest {

    @Mock
    private ParkingSessionRepository sessionRepository;

    @InjectMocks
    private RevenueService revenueService;

    @Test
    void getRevenue_shouldReturnSumForSectorAndDate() {
        when(sessionRepository.sumAmountBySectorAndDate(eq("A"), any(Instant.class), any(Instant.class)))
                .thenReturn(new BigDecimal("150.00"));

        final RevenueRequest request = new RevenueRequest(LocalDate.of(2025, 1, 1), "A");
        final RevenueResponse response = revenueService.getRevenue(request);

        assertEquals(new BigDecimal("150.00"), response.amount());
        assertEquals("BRL", response.currency());
    }

    @Test
    void getRevenue_shouldReturnZeroWhenNoSessions() {
        when(sessionRepository.sumAmountBySectorAndDate(eq("B"), any(Instant.class), any(Instant.class)))
                .thenReturn(BigDecimal.ZERO);

        final RevenueRequest request = new RevenueRequest(LocalDate.of(2025, 1, 1), "B");
        final RevenueResponse response = revenueService.getRevenue(request);

        assertEquals(BigDecimal.ZERO, response.amount());
    }
}
