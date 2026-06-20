package com.qualitysports.backend.heka.dto;

import java.math.BigDecimal;

public record HekaCreateGuideRequest(
        String distributorId,
        String cityDestination,
        BigDecimal declaredValue,
        BigDecimal total,
        int weight,
        int height,
        int longDim,
        int width,
        BigDecimal collectionValue,
        String product,
        String note
) {}
