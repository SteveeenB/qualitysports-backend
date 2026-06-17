package com.qualitysports.backend.admin.service;

import com.qualitysports.backend.admin.dto.*;
import com.qualitysports.backend.admin.entity.ReglaPaquete;
import com.qualitysports.backend.admin.repository.ReglaPaqueteRepository;
import com.qualitysports.backend.pedido.repository.PedidoRepository;
import com.qualitysports.backend.pedido.service.PedidoService;
import com.qualitysports.backend.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ClienteRepository clienteRepository;
    private final AsesorVentasRepository asesorVentasRepository;
    private final UserRepository userRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoService pedidoService;
    private final ReglaPaqueteRepository reglaPaqueteRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<ClienteAdminDTO> listarClientes() {
        return clienteRepository.findAll().stream()
                .map(c -> new ClienteAdminDTO(c.getId(), c.getNombre(), c.getEmail(),
                        c.getTelefono(), c.getDireccionEnvio(), c.isActivo(), c.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AsesorDTO> listarAsesores() {
        return asesorVentasRepository.findAll().stream()
                .map(a -> new AsesorDTO(a.getId(), a.getNombre(), a.getEmail(),
                        a.getTelefono(), a.isActivo()))
                .toList();
    }

    @Transactional
    public AsesorDTO crearAsesor(CrearAsesorRequest req) {
        if (userRepository.existsByEmail(req.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo.");
        AsesorVentas asesor = AsesorVentas.builder()
                .nombre(req.nombre())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .telefono(req.telefono())
                .role(Role.ASESOR_VENTAS)
                .activo(true)
                .build();
        asesor = asesorVentasRepository.save(asesor);
        return new AsesorDTO(asesor.getId(), asesor.getNombre(), asesor.getEmail(), asesor.getTelefono(), asesor.isActivo());
    }

    @Transactional
    public AsesorDTO actualizarAsesor(Long id, ActualizarAsesorRequest req) {
        AsesorVentas asesor = asesorVentasRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asesor no encontrado."));
        if (req.nombre()   != null) asesor.setNombre(req.nombre());
        if (req.email()    != null) asesor.setEmail(req.email());
        if (req.telefono() != null) asesor.setTelefono(req.telefono());
        if (req.activo()   != null) asesor.setActivo(req.activo());
        asesor = asesorVentasRepository.save(asesor);
        return new AsesorDTO(asesor.getId(), asesor.getNombre(), asesor.getEmail(), asesor.getTelefono(), asesor.isActivo());
    }

    @Transactional
    public void desactivarAsesor(Long id) {
        AsesorVentas asesor = asesorVentasRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asesor no encontrado."));
        asesor.setActivo(false);
        asesorVentasRepository.save(asesor);
    }

    @Transactional
    public ClienteAdminDTO crearCliente(CrearClienteRequest req) {
        if (userRepository.existsByEmail(req.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo.");
        Cliente cliente = Cliente.builder()
                .nombre(req.nombre())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .telefono(req.telefono())
                .direccionEnvio(req.direccionEnvio())
                .role(Role.CLIENTE)
                .activo(true)
                .build();
        cliente = clienteRepository.save(cliente);
        return new ClienteAdminDTO(cliente.getId(), cliente.getNombre(), cliente.getEmail(),
                cliente.getTelefono(), cliente.getDireccionEnvio(), cliente.isActivo(), cliente.getCreatedAt());
    }

    @Transactional
    public ClienteAdminDTO actualizarCliente(Long id, ActualizarClienteRequest req) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado."));
        if (req.nombre()         != null) cliente.setNombre(req.nombre());
        if (req.email()          != null) cliente.setEmail(req.email());
        if (req.telefono()       != null) cliente.setTelefono(req.telefono());
        if (req.direccionEnvio() != null) cliente.setDireccionEnvio(req.direccionEnvio());
        cliente = clienteRepository.save(cliente);
        return new ClienteAdminDTO(cliente.getId(), cliente.getNombre(), cliente.getEmail(),
                cliente.getTelefono(), cliente.getDireccionEnvio(), cliente.isActivo(), cliente.getCreatedAt());
    }

    @Transactional
    public void desactivarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado."));
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public KpiResponse obtenerKpis() {
        long totalPedidos = pedidoRepository.count();
        BigDecimal ventas = pedidoRepository.sumVentasRealizadas();
        if (ventas == null) ventas = BigDecimal.ZERO;
        long clientes = clienteRepository.count();
        long pendientes = pedidoRepository.countByEstadoActual_NombreEstadoIn(
                List.of("Por confirmar", "Confirmado"));

        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (Object[] row : pedidoRepository.countByEstado()) {
            if (row[0] instanceof String estado && row[1] instanceof Long count) {
                porEstado.put(estado, count);
            }
        }

        return new KpiResponse(totalPedidos, ventas, clientes, pendientes, porEstado);
    }

    // ── Reglas de paquete ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ReglaPaqueteDTO> listarReglas() {
        return reglaPaqueteRepository.findAllByActivoTrueOrderByCantidadParesAsc().stream()
                .map(r -> new ReglaPaqueteDTO(r.getId(), r.getCantidadPares(), r.getPrecioTotalPaquete()))
                .toList();
    }

    @Transactional
    public ReglaPaqueteDTO crearRegla(ReglaPaqueteDTO dto) {
        ReglaPaquete regla = ReglaPaquete.builder()
                .cantidadPares(dto.cantidadPares())
                .precioTotalPaquete(dto.precioTotalPaquete())
                .build();
        regla = reglaPaqueteRepository.save(regla);
        return new ReglaPaqueteDTO(regla.getId(), regla.getCantidadPares(), regla.getPrecioTotalPaquete());
    }

    @Transactional
    public ReglaPaqueteDTO actualizarRegla(Long id, ReglaPaqueteDTO dto) {
        ReglaPaquete regla = reglaPaqueteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Regla no encontrada"));
        if (dto.cantidadPares() != null) regla.setCantidadPares(dto.cantidadPares());
        if (dto.precioTotalPaquete() != null) regla.setPrecioTotalPaquete(dto.precioTotalPaquete());
        regla = reglaPaqueteRepository.save(regla);
        return new ReglaPaqueteDTO(regla.getId(), regla.getCantidadPares(), regla.getPrecioTotalPaquete());
    }

    @Transactional
    public void eliminarRegla(Long id) {
        ReglaPaquete regla = reglaPaqueteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Regla no encontrada"));
        regla.setActivo(false);
        reglaPaqueteRepository.save(regla);
    }
}
