package com.qualitysports.backend.user.dto;

public record PerfilResponse(
        Long id,
        String nombre,
        String email,
        String telefono,
        String direccionEnvio
) {
}
