package com.qualitysports.backend.config;

import com.qualitysports.backend.admin.entity.ReglaPaquete;
import com.qualitysports.backend.admin.repository.ReglaPaqueteRepository;
import com.qualitysports.backend.pedido.entity.EstadoPedido;
import com.qualitysports.backend.pedido.repository.EstadoPedidoRepository;
import com.qualitysports.backend.producto.entity.Categoria;
import com.qualitysports.backend.producto.repository.CategoriaRepository;
import com.qualitysports.backend.user.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final EstadoPedidoRepository estadoPedidoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ReglaPaqueteRepository reglaPaqueteRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedEstados();
        seedCategorias();
        seedReglasPaquete();
        seedUsuarios();
    }

    private void seedEstados() {
        List<String> estados = List.of(
                "Por confirmar", "Confirmado", "En despacho", "Entregado", "Devuelto");
        for (String nombre : estados) {
            if (estadoPedidoRepository.findByNombreEstado(nombre).isEmpty()) {
                estadoPedidoRepository.save(EstadoPedido.builder().nombreEstado(nombre).build());
                log.info("Estado creado: {}", nombre);
            }
        }
    }

    private void seedCategorias() {
        List<String> cats = List.of("Deportivo", "Casual", "Running");
        for (String nombre : cats) {
            if (!categoriaRepository.existsByNombreCategoria(nombre)) {
                categoriaRepository.save(Categoria.builder().nombreCategoria(nombre).build());
                log.info("Categoría creada: {}", nombre);
            }
        }
    }

    private void seedReglasPaquete() {
        Map<Integer, BigDecimal> defaults = Map.of(
                2, new BigDecimal("190000"),
                3, new BigDecimal("280000"),
                4, new BigDecimal("360000"),
                5, new BigDecimal("450000")
        );
        defaults.forEach((cantidad, precio) -> {
            if (reglaPaqueteRepository.findByCantidadParesAndActivoTrue(cantidad).isEmpty()) {
                reglaPaqueteRepository.save(ReglaPaquete.builder()
                        .cantidadPares(cantidad)
                        .precioTotalPaquete(precio)
                        .build());
                log.info("Regla paquete: {} pares -> ${}", cantidad, precio.toPlainString());
            }
        });
    }

    private void seedUsuarios() {
        if (!userRepository.existsByEmail("admin@qualitysports.com")) {
            Administrador admin = Administrador.builder()
                    .nombre("Administrador")
                    .email("admin@qualitysports.com")
                    .password(passwordEncoder.encode("Admin2026*"))
                    .role(Role.ADMINISTRADOR)
                    .activo(true)
                    .departamento("Dirección General")
                    .build();
            userRepository.save(admin);
            log.info("Admin creado: admin@qualitysports.com / Admin2026*");
        }

        if (!userRepository.existsByEmail("asesor1@qualitysports.com")) {
            AsesorVentas a1 = AsesorVentas.builder()
                    .nombre("Asesor Uno")
                    .email("asesor1@qualitysports.com")
                    .password(passwordEncoder.encode("Asesor2026*"))
                    .role(Role.ASESOR_VENTAS)
                    .activo(true)
                    .telefono("573001000001")
                    .build();
            userRepository.save(a1);
            log.info("Asesor 1 creado: asesor1@qualitysports.com / Asesor2026*");
        }

        if (!userRepository.existsByEmail("asesor2@qualitysports.com")) {
            AsesorVentas a2 = AsesorVentas.builder()
                    .nombre("Asesor Dos")
                    .email("asesor2@qualitysports.com")
                    .password(passwordEncoder.encode("Asesor2026*"))
                    .role(Role.ASESOR_VENTAS)
                    .activo(true)
                    .telefono("573001000002")
                    .build();
            userRepository.save(a2);
            log.info("Asesor 2 creado: asesor2@qualitysports.com / Asesor2026*");
        }
    }
}
