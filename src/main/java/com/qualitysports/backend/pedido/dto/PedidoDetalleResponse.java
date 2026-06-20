package com.qualitysports.backend.pedido.dto;

import java.math.BigDecimal;

public record PedidoDetalleResponse(
        Long productoId,
        String productoNombre,
        String imagenUrl,
        Integer talla,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotalItem
) {}
