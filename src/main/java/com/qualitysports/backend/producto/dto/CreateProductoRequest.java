package com.qualitysports.backend.producto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Set;

public record CreateProductoRequest(
        @NotBlank String nombre,
        String descripcion,
        @NotNull @DecimalMin("0.01") BigDecimal precioBase,
        String imagenUrl,
        Long categoriaId,
        Set<Integer> tallasDisponibles
) {}
