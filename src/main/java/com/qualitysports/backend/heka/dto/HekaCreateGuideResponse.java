package com.qualitysports.backend.heka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HekaCreateGuideResponse(
        int code,
        GuideData response
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GuideData(
            @JsonProperty("shipment_id")   String shipmentId,
            @JsonProperty("guide_number")  String guideNumber,
            @JsonProperty("status")        String status
    ) {}
}
