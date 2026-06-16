package com.qualitysports.backend.pedido.dto;

import com.qualitysports.backend.pedido.entity.ModalidadEntrega;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        String compradorNombre,
        String compradorApellido,
        String compradorTelefono,
        ModalidadEntrega modalidadEntrega,
        String direccionEnvio,
        String municipio,
        String departamento,
        String estadoActual,
        String asesorNombre,
        LocalDateTime fecha,
        BigDecimal subtotal,
        BigDecimal descuentoAplicado,
        BigDecimal totalNeto,
        List<PedidoDetalleResponse> detalles
) {}
