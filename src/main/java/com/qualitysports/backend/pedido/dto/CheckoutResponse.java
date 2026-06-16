package com.qualitysports.backend.pedido.dto;

import java.math.BigDecimal;

public record CheckoutResponse(
        Long pedidoId,
        String compradorNombre,
        String compradorApellido,
        BigDecimal subtotal,
        BigDecimal descuentoAplicado,
        BigDecimal totalNeto,
        String whatsappUrl
) {}
