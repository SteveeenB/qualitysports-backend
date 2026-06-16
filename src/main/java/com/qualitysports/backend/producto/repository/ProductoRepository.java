package com.qualitysports.backend.producto.repository;

import com.qualitysports.backend.producto.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Page<Producto> findByActivoTrue(Pageable pageable);

    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String q);

    List<Producto> findByCategoria_IdAndActivoTrue(Long categoriaId);

    Optional<Producto> findByIdAndActivoTrue(Long id);
}
