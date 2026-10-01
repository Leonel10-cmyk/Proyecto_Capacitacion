package com.capacitacion.ebdn.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class KeepAliveService {

    @Value("${app.url:}")
    private String appUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Auto-ping cada 10 minutos (600,000 ms) para evitar que Render
     * suspenda la instancia gratuita por inactividad.
     */
    @Scheduled(initialDelay = 60000, fixedRate = 600000)
    public void autoPing() {
        if (appUrl == null || appUrl.trim().isEmpty()) {
            return;
        }

        try {
            String pingUrl = appUrl.trim();
            if (!pingUrl.endsWith("/api/public/ping")) {
                pingUrl = pingUrl.replaceAll("/+$", "") + "/api/public/ping";
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(pingUrl))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("🤖 Auto-ping OK: " + response.statusCode());
            } else {
                System.out.println("⚠️ Auto-ping respondió con estado: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error en Auto-ping: " + e.getMessage());
        }
    }
}
