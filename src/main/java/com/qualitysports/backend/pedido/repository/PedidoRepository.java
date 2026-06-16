package com.qualitysports.backend.pedido.repository;

import com.qualitysports.backend.pedido.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByCliente_Id(Long clienteId);

    List<Pedido> findByAsesor_Id(Long asesorId);

    Optional<Pedido> findByIdAndCliente_Id(Long pedidoId, Long clienteId);

    @Query("SELECT SUM(p.totalNeto) FROM Pedido p WHERE p.estadoActual.nombreEstado <> 'Devuelto'")
    BigDecimal sumVentasRealizadas();

    @Query("SELECT p.estadoActual.nombreEstado, COUNT(p) FROM Pedido p GROUP BY p.estadoActual.nombreEstado")
    List<Object[]> countByEstado();

    long countByEstadoActual_NombreEstadoIn(List<String> estados);
}
