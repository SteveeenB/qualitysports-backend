package com.qualitysports.backend.pedido.repository;

import com.qualitysports.backend.pedido.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // JOIN FETCH evita N+1 al listar pedidos con detalles/productos
    @Query("SELECT DISTINCT p FROM Pedido p " +
           "LEFT JOIN FETCH p.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "LEFT JOIN FETCH p.estadoActual " +
           "LEFT JOIN FETCH p.asesor " +
           "ORDER BY p.fecha DESC")
    List<Pedido> findAllConDetalles();

    @Query("SELECT DISTINCT p FROM Pedido p " +
           "LEFT JOIN FETCH p.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "LEFT JOIN FETCH p.estadoActual " +
           "LEFT JOIN FETCH p.asesor " +
           "WHERE p.cliente.id = :clienteId " +
           "ORDER BY p.fecha DESC")
    List<Pedido> findByClienteIdConDetalles(@Param("clienteId") Long clienteId);

    @Query("SELECT DISTINCT p FROM Pedido p " +
           "LEFT JOIN FETCH p.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "LEFT JOIN FETCH p.estadoActual " +
           "LEFT JOIN FETCH p.asesor " +
           "WHERE p.asesor.id = :asesorId " +
           "ORDER BY p.fecha DESC")
    List<Pedido> findByAsesorIdConDetalles(@Param("asesorId") Long asesorId);

    List<Pedido> findByCliente_Id(Long clienteId);

    List<Pedido> findByAsesor_Id(Long asesorId);

    Optional<Pedido> findByIdAndCliente_Id(Long pedidoId, Long clienteId);

    @Query("SELECT SUM(p.totalNeto) FROM Pedido p WHERE p.estadoActual.nombreEstado <> 'Devuelto'")
    BigDecimal sumVentasRealizadas();

    @Query("SELECT p.estadoActual.nombreEstado, COUNT(p) FROM Pedido p GROUP BY p.estadoActual.nombreEstado")
    List<Object[]> countByEstado();

    long countByEstadoActual_NombreEstadoIn(List<String> estados);
}
