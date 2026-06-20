package com.qualitysports.backend.heka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HekaCityResponse(
        int code,
        List<CityItem> response
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CityItem(
            @JsonProperty("_id")   String id,
            @JsonProperty("label") String label,
            @JsonProperty("dane")  String dane
    ) {}
}
