package com.qualitysports.backend.producto.repository;

import com.qualitysports.backend.producto.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreCategoria(String nombreCategoria);
}
