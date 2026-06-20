package com.qualitysports.backend.heka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HekaQuoteResponse(
        int code,
        List<CarrierQuote> response
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CarrierQuote(
            @JsonProperty("distributor_id")  String distributorId,
            @JsonProperty("name")            String name,
            @JsonProperty("total")           BigDecimal total,
            @JsonProperty("days")            String days,
            @JsonProperty("commission")      BigDecimal commission
    ) {}
}
