package com.qualitysports.backend.pedido.dto;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutResponse(
        Long pedidoId,
        String compradorNombre,
        String compradorApellido,
        BigDecimal subtotal,
        BigDecimal descuentoAplicado,
        BigDecimal totalNeto,
        String whatsappUrl,
        List<CheckoutItemResponse> items
) {}
