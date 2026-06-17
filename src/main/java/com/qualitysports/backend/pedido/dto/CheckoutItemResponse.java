package com.qualitysports.backend.pedido.dto;

import java.math.BigDecimal;

public record CheckoutItemResponse(
        String nombreProducto,
        String imagenUrl,
        Integer talla,
        Integer cantidad,
        BigDecimal precioUnitario
) {}
