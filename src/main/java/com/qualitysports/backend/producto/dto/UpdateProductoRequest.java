package com.qualitysports.backend.producto.dto;

import java.math.BigDecimal;
import java.util.Set;

public record UpdateProductoRequest(
        String nombre,
        String descripcion,
        BigDecimal precioBase,
        String imagenUrl,
        Long categoriaId,
        Set<Integer> tallasDisponibles
) {}
