package com.qualitysports.backend.pedido.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estados_pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombreEstado;
}
