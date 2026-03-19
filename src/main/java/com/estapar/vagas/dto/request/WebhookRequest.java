package com.estapar.vagas.dto.request;

import com.estapar.vagas.domain.enums.EventType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record WebhookRequest(
        @JsonProperty("license_plate")
        @NotBlank
        String licensePlate,

        @JsonProperty("entry_time")
        Instant entryTime,

        @JsonProperty("exit_time")
        Instant exitTime,

        Double lat,

        Double lng,

        @JsonProperty("event_type")
        @NotNull
        EventType eventType
) {}
