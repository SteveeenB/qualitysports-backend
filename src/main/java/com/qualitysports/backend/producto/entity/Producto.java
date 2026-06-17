package com.qualitysports.backend.producto.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    private String imagenUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modelo_id")
    private Modelo modelo;

    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "producto_tallas", joinColumns = @JoinColumn(name = "producto_id"))
    @Column(name = "talla")
    private Set<Integer> tallasDisponibles = new HashSet<>();
}
