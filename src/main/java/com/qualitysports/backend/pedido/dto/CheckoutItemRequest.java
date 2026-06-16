package com.qualitysports.backend.pedido.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CheckoutItemRequest(
        @NotNull Long productoId,
        @NotNull Integer talla,
        @NotNull @Min(1) Integer cantidad
) {}
