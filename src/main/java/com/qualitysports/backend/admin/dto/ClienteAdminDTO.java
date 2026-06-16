package com.qualitysports.backend.admin.dto;

import java.time.LocalDateTime;

public record ClienteAdminDTO(
        Long id,
        String nombre,
        String email,
        String telefono,
        String direccionEnvio,
        boolean activo,
        LocalDateTime createdAt
) {}
