package com.estapar.vagas.controller;

import com.estapar.vagas.dto.request.RevenueRequest;
import com.estapar.vagas.dto.response.RevenueResponse;
import com.estapar.vagas.service.RevenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/revenue")
@RequiredArgsConstructor
@Tag(name = "Revenue", description = "Parking revenue queries")
public class RevenueController {

    private final RevenueService revenueService;

    @GetMapping
    @Operation(summary = "Get total revenue by sector and date")
    public ResponseEntity<RevenueResponse> getRevenue(
            @Parameter(description = "Date in format YYYY-MM-DD", example = "2025-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Sector name", example = "A")
            @RequestParam String sector) {
        return ResponseEntity.ok(revenueService.getRevenue(new RevenueRequest(date, sector)));
    }
}
