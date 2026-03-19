package com.estapar.vagas.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record GarageConfigResponse(
        List<SectorConfig> garage,
        List<SpotConfig> spots
) {
    public record SectorConfig(
            String sector,
            @JsonProperty("basePrice")
            @JsonAlias({"base_price", "BasePrice", "baseprice"})
            BigDecimal basePrice,
            @JsonProperty("max_capacity")
            @JsonAlias({"maxCapacity", "MaxCapacity"})
            int maxCapacity
    ) {}

    public record SpotConfig(
            Long id,
            String sector,
            double lat,
            double lng
    ) {}
}
