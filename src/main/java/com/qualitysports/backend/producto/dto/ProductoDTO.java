package com.qualitysports.backend.producto.dto;

import java.math.BigDecimal;
import java.util.Set;

public record ProductoDTO(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precioBase,
        String imagenUrl,
        CategoriaDTO categoria,
        ModeloDTO modelo,
        boolean activo,
        Set<Integer> tallasDisponibles
) {}
