package com.qualitysports.backend.admin.service;

import com.qualitysports.backend.admin.dto.*;
import com.qualitysports.backend.admin.entity.ReglaPaquete;
import com.qualitysports.backend.admin.repository.ReglaPaqueteRepository;
import com.qualitysports.backend.pedido.repository.PedidoRepository;
import com.qualitysports.backend.pedido.service.PedidoService;
import com.qualitysports.backend.user.AsesorVentasRepository;
import com.qualitysports.backend.user.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final PedidoRepository pedidoRepository;
    private final PedidoService pedidoService;
    private final ReglaPaqueteRepository reglaPaqueteRepository;

    @Transactional(readOnly = true)
    public List<ClienteAdminDTO> listarClientes() {
        return clienteRepository.findAll().stream()
                .map(c -> new ClienteAdminDTO(c.getId(), c.getNombre(), c.getEmail(),
                        c.getTelefono(), c.getDireccionEnvio(), c.isActivo(), c.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AsesorDTO> listarAsesores() {
        return asesorVentasRepository.findAllByActivoTrue().stream()
                .map(a -> new AsesorDTO(a.getId(), a.getNombre(), a.getEmail(),
                        a.getTelefono(), a.getZonaAsignada()))
                .toList();
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
