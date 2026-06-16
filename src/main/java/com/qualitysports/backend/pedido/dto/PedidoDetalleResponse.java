package com.qualitysports.backend.pedido.dto;

import java.math.BigDecimal;

public record PedidoDetalleResponse(
        Long productoId,
        String productoNombre,
        Integer talla,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotalItem
) {}
