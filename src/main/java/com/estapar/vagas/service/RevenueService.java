package com.estapar.vagas.service;

import com.estapar.vagas.dto.request.RevenueRequest;
import com.estapar.vagas.dto.response.RevenueResponse;
import com.estapar.vagas.repository.ParkingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevenueService {

    private final ParkingSessionRepository sessionRepository;

    @Transactional(readOnly = true)
    public RevenueResponse getRevenue(RevenueRequest request) {
        final Instant startOfDay = request.date().atStartOfDay().toInstant(ZoneOffset.UTC);
        final Instant endOfDay = request.date().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        final BigDecimal total = sessionRepository.sumAmountBySectorAndDate(
                request.sector(), startOfDay, endOfDay);

        log.info("Revenue query: sector={}, date={}, total={}", request.sector(), request.date(), total);
        return RevenueResponse.of(total);
    }
}
