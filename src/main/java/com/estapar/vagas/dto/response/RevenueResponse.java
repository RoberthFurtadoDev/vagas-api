package com.estapar.vagas.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

public record RevenueResponse(
        BigDecimal amount,
        String currency,
        @JsonProperty("timestamp")
        Instant timestamp
) {
    public static RevenueResponse of(BigDecimal amount) {
        return new RevenueResponse(amount, "BRL", Instant.now());
    }
}
