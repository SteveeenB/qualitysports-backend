package com.qualitysports.backend.heka.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qualitysports.backend.heka.config.HekaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HekaTokenService {

    private final HekaProperties props;
    private final RestTemplate restTemplate;

    private String cachedToken;
    private LocalDateTime tokenExpiry;

    public synchronized String getToken() {
        if (cachedToken == null || LocalDateTime.now().isAfter(tokenExpiry.minusHours(1))) {
            refresh();
        }
        return cachedToken;
    }

    private void refresh() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-Key", props.getApiKey());

        Map<String, String> body = Map.of(
                "email",    props.getEmail(),
                "password", props.getPassword(),
                "channel",  props.getChannel()
        );

        ResponseEntity<LoginResponse> resp = restTemplate.exchange(
                props.getBaseUrl() + "/api/v1/user/login",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                LoginResponse.class
        );

        if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
            cachedToken = resp.getBody().response().token();
            tokenExpiry = LocalDateTime.now().plusDays(7);
        } else {
            throw new RuntimeException("Error al autenticar con HekaEntrega");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record LoginResponse(TokenData response) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TokenData(@JsonProperty("token") String token) {}
}
