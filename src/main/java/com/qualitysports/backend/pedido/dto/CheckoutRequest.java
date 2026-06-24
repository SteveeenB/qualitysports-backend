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
        String compradorEmail,
        @NotNull ModalidadEntrega modalidadEntrega,
        String direccionEnvio,
        String barrio,
        String municipio,
        String departamento,
        @NotEmpty @Valid List<CheckoutItemRequest> items,
        String cityDane,
        String fbp,
        String fbc
) {}
