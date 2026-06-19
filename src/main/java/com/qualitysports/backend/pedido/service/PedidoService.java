package com.qualitysports.backend.pedido.service;

import com.qualitysports.backend.admin.entity.ReglaPaquete;
import com.qualitysports.backend.admin.repository.ReglaPaqueteRepository;
import com.qualitysports.backend.pedido.dto.*;
import com.qualitysports.backend.pedido.entity.*;
import com.qualitysports.backend.pedido.repository.*;
import com.qualitysports.backend.producto.entity.Producto;
import com.qualitysports.backend.producto.repository.ProductoRepository;
import com.qualitysports.backend.user.AsesorVentas;
import com.qualitysports.backend.user.AsesorVentasRepository;
import com.qualitysports.backend.user.Cliente;
import com.qualitysports.backend.user.User;
import com.qualitysports.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoDetalleRepository pedidoDetalleRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final ProductoRepository productoRepository;
    private final AsesorVentasRepository asesorVentasRepository;
    private final ReglaPaqueteRepository reglaPaqueteRepository;
    private final UserRepository userRepository; // BUG 2: para registrar auditoría de admin

    private static final List<String> CADENA_ESTADOS =
            List.of("Por confirmar", "Confirmado", "En despacho", "Entregado", "Devuelto");

    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    @Transactional
    public CheckoutResponse crearPedido(CheckoutRequest req, Cliente clienteAutenticado) {
        if (req.modalidadEntrega() == ModalidadEntrega.DOMICILIO
                && (req.direccionEnvio() == null || req.direccionEnvio().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La dirección de envío es obligatoria para modalidad DOMICILIO");
        }

        // BUG 4 FIX: validar y cachear productos en un solo loop
        Map<Long, Producto> productosCache = new HashMap<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalPares = 0;

        for (CheckoutItemRequest item : req.items()) {
            Producto producto = productoRepository.findByIdAndActivoTrue(item.productoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Producto no encontrado: " + item.productoId()));
            if (!producto.getTallasDisponibles().contains(item.talla())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Talla " + item.talla() + " no disponible para el producto " + producto.getNombre());
            }
            productosCache.put(item.productoId(), producto);
            subtotal = subtotal.add(producto.getPrecioBase().multiply(BigDecimal.valueOf(item.cantidad())));
            totalPares += item.cantidad();
        }

        // Aplicar regla de paquete
        BigDecimal[] totales = calcularTotales(subtotal, totalPares);
        BigDecimal descuentoAplicado = totales[0];
        BigDecimal totalNeto = totales[1];

        // Asignar asesor round-robin
        AsesorVentas asesor = asignarAsesor();

        // Estado inicial
        EstadoPedido estadoInicial = estadoPedidoRepository.findByNombreEstado("Por confirmar")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Estado 'Por confirmar' no encontrado. Ejecute el seeder."));

        // Crear pedido
        Pedido pedido = Pedido.builder()
                .cliente(clienteAutenticado)
                .asesor(asesor)
                .subtotal(subtotal)
                .descuentoAplicado(descuentoAplicado)
                .totalNeto(totalNeto)
                .estadoActual(estadoInicial)
                .compradorNombre(req.compradorNombre())
                .compradorApellido(req.compradorApellido())
                .compradorCedula(req.compradorCedula())
                .compradorTelefono(req.compradorTelefono())
                .compradorEmail(req.compradorEmail())
                .modalidadEntrega(req.modalidadEntrega())
                .direccionEnvio(req.direccionEnvio())
                .barrio(req.barrio())
                .municipio(req.municipio())
                .departamento(req.departamento())
                .build();
        pedidoRepository.save(pedido);

        // Persistir detalles usando el cache (sin doble query)
        for (CheckoutItemRequest item : req.items()) {
            Producto producto = productosCache.get(item.productoId());
            PedidoDetalle detalle = PedidoDetalle.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .cantidad(item.cantidad())
                    .tallaSeleccionada(item.talla())
                    .precioUnitario(producto.getPrecioBase())
                    .build();
            pedidoDetalleRepository.save(detalle);
        }

        // Registrar historial inicial
        historialEstadoRepository.save(HistorialEstado.builder()
                .pedido(pedido)
                .modificadoPor(null)
                .estadoAnterior(null)
                .estadoNuevo(estadoInicial)
                .observaciones("Pedido creado")
                .build());

        List<CheckoutItemResponse> itemsResp = req.items().stream()
                .map(i -> {
                    Producto p = productosCache.get(i.productoId());
                    return new CheckoutItemResponse(
                            p.getNombre(), p.getImagenUrl(), i.talla(), i.cantidad(), p.getPrecioBase());
                }).toList();

        String whatsappUrl = generarUrlWhatsApp(asesor, pedido, req.items(), productosCache);

        return new CheckoutResponse(pedido.getId(),
                pedido.getCompradorNombre(), pedido.getCompradorApellido(),
                subtotal, descuentoAplicado, totalNeto, whatsappUrl, itemsResp);
    }

    // BUG 1 FIX: @Transactional(readOnly=true) en todos los métodos de lectura
    @Transactional(readOnly = true)
    public List<PedidoResponse> pedidosDeCliente(Long clienteId) {
        return pedidoRepository.findByCliente_Id(clienteId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse pedidoDeCliente(Long pedidoId, Long clienteId) {
        return pedidoRepository.findByIdAndCliente_Id(pedidoId, clienteId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> pedidosDeAsesor(Long asesorId) {
        return pedidoRepository.findByAsesor_Id(asesorId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse pedidoDeAsesor(Long pedidoId, Long asesorId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        if (!pedido.getAsesor().getId().equals(asesorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este pedido");
        }
        return toResponse(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> todosLosPedidos() {
        return pedidoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse cualquierPedido(Long pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    }

    @Transactional
    public PedidoResponse cambiarEstado(Long pedidoId, Long usuarioId, CambiarEstadoRequest req) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

        String estadoActualNombre = pedido.getEstadoActual().getNombreEstado();
        int idx = CADENA_ESTADOS.indexOf(estadoActualNombre);
        if (idx == -1 || idx == CADENA_ESTADOS.size() - 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El pedido está en estado terminal: " + estadoActualNombre);
        }
        String siguienteNombre = CADENA_ESTADOS.get(idx + 1);
        EstadoPedido estadoAnterior = pedido.getEstadoActual();
        EstadoPedido estadoNuevo = estadoPedidoRepository.findByNombreEstado(siguienteNombre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Estado '" + siguienteNombre + "' no configurado"));

        // BUG 2 FIX: usar UserRepository para cubrir tanto asesores como admins
        User modificadoPor = userRepository.findById(usuarioId).orElse(null);

        pedido.setEstadoActual(estadoNuevo);
        pedidoRepository.save(pedido);

        historialEstadoRepository.save(HistorialEstado.builder()
                .pedido(pedido)
                .modificadoPor(modificadoPor)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .observaciones(req.observaciones())
                .build());

        return toResponse(pedido);
    }

    @Transactional
    public PedidoResponse reasignarAsesor(Long pedidoId, Long nuevoAsesorId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

        String estadoNombre = pedido.getEstadoActual().getNombreEstado();
        if (!List.of("Por confirmar", "Confirmado").contains(estadoNombre)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No se puede reasignar en estado: " + estadoNombre);
        }
        AsesorVentas nuevoAsesor = asesorVentasRepository.findById(nuevoAsesorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asesor no encontrado"));
        pedido.setAsesor(nuevoAsesor);
        return toResponse(pedidoRepository.save(pedido));
    }

    // ── Lógica interna ───────────────────────────────────────────────────────

    private BigDecimal[] calcularTotales(BigDecimal subtotal, int totalPares) {
        Optional<ReglaPaquete> regla = reglaPaqueteRepository
                .findTopByCantidadParesLessThanEqualAndActivoTrueOrderByCantidadParesDesc(totalPares);
        if (regla.isPresent()) {
            BigDecimal precioPorPar = regla.get().getPrecioTotalPaquete()
                    .divide(BigDecimal.valueOf(regla.get().getCantidadPares()), 2, RoundingMode.HALF_UP);
            BigDecimal totalNeto = precioPorPar.multiply(BigDecimal.valueOf(totalPares));
            BigDecimal descuento = subtotal.subtract(totalNeto).max(BigDecimal.ZERO);
            return new BigDecimal[]{descuento, totalNeto};
        }
        return new BigDecimal[]{BigDecimal.ZERO, subtotal};
    }

    @Transactional(readOnly = true)
    public List<com.qualitysports.backend.admin.dto.ReglaPaqueteDTO> listarReglas() {
        return reglaPaqueteRepository.findAllByActivoTrueOrderByCantidadParesAsc().stream()
                .map(r -> new com.qualitysports.backend.admin.dto.ReglaPaqueteDTO(
                        r.getId(), r.getCantidadPares(), r.getPrecioTotalPaquete()))
                .toList();
    }

    private AsesorVentas asignarAsesor() {
        List<AsesorVentas> asesores = asesorVentasRepository.findAllByActivoTrue();
        if (asesores.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No hay asesores disponibles");
        }
        // BUG 3 FIX: Math.floorMod garantiza resultado no negativo aunque haya overflow
        int idx = Math.floorMod(roundRobinIndex.getAndIncrement(), asesores.size());
        return asesores.get(idx);
    }

    private String generarUrlWhatsApp(AsesorVentas asesor, Pedido pedido,
            List<CheckoutItemRequest> items, Map<Long, Producto> productosCache) {

        // ── Bloque cliente ────────────────────────────────────────────────────
        StringBuilder cliente = new StringBuilder();
        cliente.append(String.format("👤 *Cliente:* %s %s%n",
                pedido.getCompradorNombre(), pedido.getCompradorApellido()));
        if (pedido.getCompradorCedula() != null && !pedido.getCompradorCedula().isBlank()) {
            cliente.append(String.format("📋 Cédula: %s%n", pedido.getCompradorCedula()));
        }
        if (pedido.getCompradorEmail() != null && !pedido.getCompradorEmail().isBlank()) {
            cliente.append(String.format("📧 %s%n", pedido.getCompradorEmail()));
        }
        cliente.append(String.format("📞 Tel: %s", pedido.getCompradorTelefono()));

        // ── Productos ─────────────────────────────────────────────────────────
        StringBuilder sb = new StringBuilder();
        for (CheckoutItemRequest item : items) {
            Producto p = productosCache.get(item.productoId());
            sb.append(String.format("• %s – T%d ×%d → $%s%n",
                    p.getNombre(), item.talla(), item.cantidad(),
                    formatPrecio(p.getPrecioBase().multiply(BigDecimal.valueOf(item.cantidad())))));
            if (p.getImagenUrl() != null && !p.getImagenUrl().isBlank()) {
                sb.append("  📸 ").append(p.getImagenUrl()).append("\n");
            }
        }

        // ── Entrega ───────────────────────────────────────────────────────────
        String entrega;
        if (pedido.getModalidadEntrega() == ModalidadEntrega.DOMICILIO) {
            StringBuilder dir = new StringBuilder("📍 ");
            dir.append(pedido.getDireccionEnvio());
            if (pedido.getBarrio() != null && !pedido.getBarrio().isBlank()) {
                dir.append(", ").append(pedido.getBarrio());
            }
            dir.append(String.format(", %s, %s", pedido.getMunicipio(), pedido.getDepartamento()));
            entrega = dir.toString();
        } else {
            entrega = "🏪 Retiro en oficina";
        }

        String descto = pedido.getDescuentoAplicado().compareTo(BigDecimal.ZERO) > 0
                ? String.format("%n💸 Descuento: -$%s", formatPrecio(pedido.getDescuentoAplicado()))
                : "";

        String msg = String.format(
                "Hola, acabo de hacer un pedido en *QualitySports* 👟%n%n" +
                "*Pedido #QS-%d*%n%n" +
                "%s%n%n" +
                "📦 *Productos:*%n%s%n" +
                "%s%n%n" +
                "💵 Subtotal: $%s%s%n" +
                "✅ *Total: $%s COP*%n%n" +
                "Quedo pendiente de la confirmación.",
                pedido.getId(),
                cliente,
                sb,
                entrega,
                formatPrecio(pedido.getSubtotal()),
                descto,
                formatPrecio(pedido.getTotalNeto())
        );

        String encoded = URLEncoder.encode(msg, StandardCharsets.UTF_8);
        return "https://wa.me/" + asesor.getTelefono() + "?text=" + encoded;
    }

    private String formatPrecio(BigDecimal v) {
        return new java.text.DecimalFormat("#,###").format(v);
    }

    public PedidoResponse toResponse(Pedido pedido) {
        List<PedidoDetalleResponse> detalles = pedido.getDetalles().stream()
                .map(d -> new PedidoDetalleResponse(
                        d.getProducto().getId(),
                        d.getProducto().getNombre(),
                        d.getTallaSeleccionada(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()))
                )).toList();

        String asesorNombre = pedido.getAsesor() != null ? pedido.getAsesor().getNombre() : null;

        return new PedidoResponse(
                pedido.getId(),
                pedido.getCompradorNombre(),
                pedido.getCompradorApellido(),
                pedido.getCompradorTelefono(),
                pedido.getModalidadEntrega(),
                pedido.getDireccionEnvio(),
                pedido.getMunicipio(),
                pedido.getDepartamento(),
                pedido.getEstadoActual().getNombreEstado(),
                asesorNombre,
                pedido.getFecha(),
                pedido.getSubtotal(),
                pedido.getDescuentoAplicado(),
                pedido.getTotalNeto(),
                detalles,
                pedido.getGuia(),
                pedido.getTransportadora()
        );
    }
}
