package com.qualitysports.backend.producto.repository;

import com.qualitysports.backend.producto.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT p FROM Producto p WHERE " +
           "(:buscar IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :buscar, '%'))) AND " +
           "(:modeloId IS NULL OR p.modelo.id = :modeloId)")
    Page<Producto> findWithFilters(
            @Param("buscar") String buscar,
            @Param("modeloId") Long modeloId,
            Pageable pageable);
}
