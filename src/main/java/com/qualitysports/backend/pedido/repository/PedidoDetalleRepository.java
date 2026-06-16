package com.qualitysports.backend.pedido.repository;

import com.qualitysports.backend.pedido.entity.PedidoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalle, Long> {
}
