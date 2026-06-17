package com.qualitysports.backend.admin.dto;

public record CrearClienteRequest(String nombre, String email, String password, String telefono, String direccionEnvio) {}
