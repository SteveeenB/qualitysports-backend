package com.qualitysports.backend.pedido.dto;

import jakarta.validation.constraints.NotNull;

public record ReasignarAsesorRequest(@NotNull Long asesorId) {}
