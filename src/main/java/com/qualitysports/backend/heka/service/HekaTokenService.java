package com.qualitysports.backend.heka.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qualitysports.backend.heka.config.HekaProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HekaTokenService {

    private final HekaProperties props;
    private final RestTemplate restTemplate;

    private volatile String cachedToken;
    private volatile LocalDateTime tokenExpiry;

    @PostConstruct
    public void init() {
        if (props.getEmail() == null || props.getEmail().isBlank()) {
            log.warn("[Heka] Credenciales no configuradas — token Heka no se obtendrá en startup");
            return;
        }
        try {
            refresh();
        } catch (Exception e) {
            log.warn("[Heka] No se pudo obtener token en startup, se reintentará en la primera llamada: {}", e.getMessage());
        }
    }

    // Renueva el token cada 6 días (expira a los 7) — nunca expira en producción
    @Scheduled(fixedRate = 6L * 24 * 60 * 60 * 1000)
    public void scheduledRefresh() {
        if (props.getEmail() == null || props.getEmail().isBlank()) return;
        try {
            log.info("[Heka] Renovando token Heka programáticamente");
            refresh();
        } catch (Exception e) {
            log.error("[Heka] Error al renovar token Heka en refresh programático: {}", e.getMessage());
        }
    }

    public synchronized String getToken() {
        if (cachedToken == null || LocalDateTime.now().isAfter(tokenExpiry.minusHours(1))) {
            refresh();
        }
        return cachedToken;
    }

    private synchronized void refresh() {
        log.info("[Heka] Autenticando — email={} url={}", props.getEmail(), props.getBaseUrl());

        if (props.getEmail() == null || props.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Credenciales Heka no configuradas (HEKA_EMAIL vacío)");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-Key", props.getApiKey());

        Map<String, String> body = Map.of(
                "email",    props.getEmail(),
                "password", props.getPassword(),
                "channel",  props.getChannel()
        );

        try {
            ResponseEntity<LoginResponse> resp = restTemplate.exchange(
                    props.getBaseUrl() + "/api/v1/user/login",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    LoginResponse.class
            );

            if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null
                    && resp.getBody().response() != null) {
                cachedToken = resp.getBody().response().token();
                tokenExpiry = LocalDateTime.now().plusDays(7);
                log.info("[Heka] Token obtenido OK, expira {}", tokenExpiry);
            } else {
                log.error("[Heka] Login OK pero sin token en la respuesta");
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "HekaEntrega no devolvió token de sesión");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RestClientException e) {
            log.error("[Heka] Error de conexión al hacer login: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo conectar con HekaEntrega: " + e.getMessage());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record LoginResponse(TokenData response) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TokenData(@JsonProperty("token") String token) {}
}
