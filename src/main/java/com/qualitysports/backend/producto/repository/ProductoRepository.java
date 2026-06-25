package com.qualitysports.backend.producto.repository;

import com.qualitysports.backend.producto.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Page<Producto> findByActivoTrue(Pageable pageable);

    Page<Producto> findByModelo_IdAndActivoTrue(Long modeloId, Pageable pageable);

    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String q);

    List<Producto> findByCategoria_IdAndActivoTrue(Long categoriaId);

    Optional<Producto> findByIdAndActivoTrue(Long id);

    Optional<Producto> findFirstByModelo_IdAndActivoTrueAndImagenUrlIsNotNull(Long modeloId);

    boolean existsByModelo_Id(Long modeloId);

    // Admin filters — called individually from the service to avoid JPQL null-type issues with PostgreSQL
    Page<Producto> findByNombreContainingIgnoreCase(String q, Pageable pageable);

    Page<Producto> findByModelo_Id(Long modeloId, Pageable pageable);

    Page<Producto> findByNombreContainingIgnoreCaseAndModelo_Id(String q, Long modeloId, Pageable pageable);
}
