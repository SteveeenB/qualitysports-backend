package com.qualitysports.backend.heka.dto;

import java.math.BigDecimal;

public record HekaQuoteRequest(
        String cityOrigin,
        String cityDestination,
        int typePayment,
        BigDecimal declaredValue,
        int weight,
        int height,
        int longDim,
        int width,
        boolean withshippingCost,
        BigDecimal collectionValue
) {}
