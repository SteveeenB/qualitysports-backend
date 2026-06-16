package com.qualitysports.backend.admin.dto;

import java.math.BigDecimal;
import java.util.Map;

public record KpiResponse(
        long totalPedidos,
        BigDecimal ventasRealizadas,
        long clientesRegistrados,
        long pedidosPendientes,
        Map<String, Long> pedidosPorEstado
) {}
