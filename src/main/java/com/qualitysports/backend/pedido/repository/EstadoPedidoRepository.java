package com.qualitysports.backend.pedido.repository;

import com.qualitysports.backend.pedido.entity.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoPedidoRepository extends JpaRepository<EstadoPedido, Long> {

    Optional<EstadoPedido> findByNombreEstado(String nombreEstado);
}
