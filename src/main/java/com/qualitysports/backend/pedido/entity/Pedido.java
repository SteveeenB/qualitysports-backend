package com.qualitysports.backend.pedido.entity;

import com.qualitysports.backend.producto.entity.Producto;
import com.qualitysports.backend.user.AsesorVentas;
import com.qualitysports.backend.user.Cliente;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asesor_id", nullable = false)
    private AsesorVentas asesor;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal descuentoAplicado = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalNeto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoPedido estadoActual;

    @Column(nullable = false)
    private String compradorNombre;

    @Column(nullable = false)
    private String compradorApellido;

    private String compradorCedula;

    @Column(nullable = false)
    private String compradorTelefono;

    private String compradorEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModalidadEntrega modalidadEntrega;

    private String direccionEnvio;

    private String barrio;

    @Column(nullable = false)
    private String municipio;

    @Column(nullable = false)
    private String departamento;

    @Builder.Default
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoDetalle> detalles = new ArrayList<>();
}
