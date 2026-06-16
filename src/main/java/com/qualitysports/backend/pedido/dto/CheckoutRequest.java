package com.qualitysports.backend.pedido.dto;

import com.qualitysports.backend.pedido.entity.ModalidadEntrega;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CheckoutRequest(
        @NotBlank String compradorNombre,
        @NotBlank String compradorApellido,
        String compradorCedula,
        @NotBlank String compradorTelefono,
        @NotNull ModalidadEntrega modalidadEntrega,
        String direccionEnvio,
        @NotBlank String municipio,
        @NotBlank String departamento,
        @NotEmpty @Valid List<CheckoutItemRequest> items
) {}
