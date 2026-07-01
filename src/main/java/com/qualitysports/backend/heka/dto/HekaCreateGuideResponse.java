package com.qualitysports.backend.heka.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HekaCreateGuideResponse(
        int code,
        GuideData response
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GuideData(
            @JsonAlias("shipment_id")  String shipmentId,
            @JsonAlias("guide_number") String guideNumber,
            String status
    ) {}
}
