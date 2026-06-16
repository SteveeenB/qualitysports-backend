package com.qualitysports.backend.pedido.entity;

import com.qualitysports.backend.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_estados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "modificado_por_id")
    private User modificadoPor;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "estado_anterior_id")
    private EstadoPedido estadoAnterior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_nuevo_id", nullable = false)
    private EstadoPedido estadoNuevo;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaCambio;

    private String observaciones;
}
