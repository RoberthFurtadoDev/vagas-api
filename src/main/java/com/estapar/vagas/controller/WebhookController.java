package com.estapar.vagas.controller;

import com.estapar.vagas.dto.request.WebhookRequest;
import com.estapar.vagas.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Tag(name = "Webhook", description = "Receives vehicle events from the simulator")
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping
    @Operation(summary = "Handle vehicle event (ENTRY, PARKED, EXIT)")
    public ResponseEntity<Void> handleEvent(@Valid @RequestBody WebhookRequest request) {
        switch (request.eventType()) {
            case ENTRY -> webhookService.handleEntry(request);
            case PARKED -> webhookService.handleParked(request);
            case EXIT -> webhookService.handleExit(request);
        }
        return ResponseEntity.ok().build();
    }
}
