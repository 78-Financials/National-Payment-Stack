package com.payaza.nps.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * Configuration for WebClient used for NPS API communication
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient npsWebClient(NpsConfiguration npsConfig) {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("nps")
                .maxConnections(100)
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofMinutes(5))
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(Duration.ofSeconds(npsConfig.getTimeoutSeconds()))
                .followRedirect(true);

        return WebClient.builder()
                .baseUrl(npsConfig.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
