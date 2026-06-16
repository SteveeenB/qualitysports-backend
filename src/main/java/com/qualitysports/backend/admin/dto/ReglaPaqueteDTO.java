package com.qualitysports.backend.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ReglaPaqueteDTO(
        Long id,
        @NotNull @Min(1) Integer cantidadPares,
        @NotNull @DecimalMin("0.01") BigDecimal precioTotalPaquete
) {}
