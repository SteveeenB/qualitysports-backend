package com.qualitysports.backend.producto.repository;

import com.qualitysports.backend.producto.entity.Modelo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModeloRepository extends JpaRepository<Modelo, Long> {
    boolean existsByNombre(String nombre);
}
