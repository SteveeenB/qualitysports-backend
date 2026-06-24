package com.qualitysports.backend.heka.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HekaQuoteResponse(
        int code,
        List<CarrierQuote> response
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CarrierQuote(
            String distributorId,
            BigDecimal total,
            @JsonAlias("deliveryTime") String days,
            BigDecimal commission
    ) {}
}
