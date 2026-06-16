package com.qualitysports.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class SupabaseStorageService {

    @Value("${supabase.project-url}")
    private String projectUrl;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${supabase.storage.bucket}")
    private String bucket;

    private final RestClient restClient = RestClient.create();

    public String upload(String fileName, byte[] bytes, String contentType) {
        String uploadUrl = projectUrl + "/storage/v1/object/" + bucket + "/" + fileName;
        try {
            restClient.post()
                    .uri(uploadUrl)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("x-upsert", "true")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(bytes)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            log.error("Supabase Storage error {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error al subir la imagen a Supabase Storage");
        }
        return projectUrl + "/storage/v1/object/public/" + bucket + "/" + fileName;
    }
}
