package com.estapar.vagas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${simulator.base-url}")
    private String simulatorBaseUrl;

    @Bean
    public RestClient simulatorRestClient() {
        return RestClient.builder()
                .baseUrl(simulatorBaseUrl)
                .build();
    }
}
