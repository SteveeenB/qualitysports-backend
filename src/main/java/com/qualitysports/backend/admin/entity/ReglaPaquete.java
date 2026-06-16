package com.qualitysports.backend.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "reglas_paquete")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReglaPaquete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer cantidadPares;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioTotalPaquete;

    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;
}
