package com.qualitysports.backend.pedido.repository;

import com.qualitysports.backend.pedido.entity.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {

    List<HistorialEstado> findByPedido_IdOrderByFechaCambioAsc(Long pedidoId);
}
