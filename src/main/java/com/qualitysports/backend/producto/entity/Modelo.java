package com.qualitysports.backend.producto.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "modelos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Modelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nombre;
}
