package com.estapar.vagas.service;

import com.estapar.vagas.domain.entity.Sector;
import com.estapar.vagas.domain.entity.Spot;
import com.estapar.vagas.dto.response.GarageConfigResponse;
import com.estapar.vagas.repository.SectorRepository;
import com.estapar.vagas.repository.SpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class GarageInitService implements ApplicationRunner {

    private final RestClient simulatorRestClient;
    private final SectorRepository sectorRepository;
    private final SpotRepository spotRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Fetching garage configuration from simulator...");
        final GarageConfigResponse config = fetchGarageConfig();
        persistSectors(config);
        persistSpots(config);
        log.info("Garage configuration loaded: {} sectors, {} spots",
                config.garage().size(), config.spots().size());
    }

    private GarageConfigResponse fetchGarageConfig() {
        final String raw = simulatorRestClient.get()
                .uri("/garage")
                .retrieve()
                .body(String.class);

        log.info("Raw simulator response: {}", raw);

        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .findAndRegisterModules()
                    .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(raw, GarageConfigResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse garage config: " + e.getMessage(), e);
        }
    }

    private void persistSectors(GarageConfigResponse config) {
        config.garage().forEach(sectorConfig -> {
            final Sector sector = sectorRepository.findById(sectorConfig.sector())
                    .orElse(new Sector());
            sector.setName(sectorConfig.sector());
            sector.setBasePrice(sectorConfig.basePrice());
            sector.setMaxCapacity(sectorConfig.maxCapacity());
            sectorRepository.save(sector);
        });
    }

    private void persistSpots(GarageConfigResponse config) {
        config.spots().forEach(spotConfig -> {
            final Sector sector = sectorRepository.getReferenceById(spotConfig.sector());
            final Spot spot = spotRepository.findById(spotConfig.id())
                    .orElse(new Spot());
            spot.setId(spotConfig.id());
            spot.setSector(sector);
            spot.setLat(spotConfig.lat());
            spot.setLng(spotConfig.lng());
            spotRepository.save(spot);
        });
    }
}
