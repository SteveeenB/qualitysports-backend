package com.qualitysports.backend.pedido.controller;

import com.qualitysports.backend.admin.dto.ReglaPaqueteDTO;
import com.qualitysports.backend.pedido.dto.*;
import com.qualitysports.backend.pedido.service.PedidoService;
import com.qualitysports.backend.user.Cliente;
import com.qualitysports.backend.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    // ── Endpoint público — descuentos ─────────────────────────────────────────

    @GetMapping("/api/descuentos")
    public List<ReglaPaqueteDTO> descuentosPublicos() {
        return pedidoService.listarReglas();
    }

    // ── Checkout público ──────────────────────────────────────────────────────

    @PostMapping("/api/pedidos")
    @ResponseStatus(HttpStatus.CREATED)
    public CheckoutResponse checkout(
            @Valid @RequestBody CheckoutRequest req,
            @AuthenticationPrincipal User usuarioAutenticado
    ) {
        Cliente cliente = (usuarioAutenticado instanceof Cliente c) ? c : null;
        return pedidoService.crearPedido(req, cliente);
    }

    // ── Rutas cliente autenticado ─────────────────────────────────────────────

    @GetMapping("/api/pedidos/mis-pedidos")
    public List<PedidoResponse> misPedidos(@AuthenticationPrincipal User usuario) {
        return pedidoService.pedidosDeCliente(usuario.getId());
    }

    @GetMapping("/api/pedidos/{id}")
    public PedidoResponse miPedido(@PathVariable Long id, @AuthenticationPrincipal User usuario) {
        return pedidoService.pedidoDeCliente(id, usuario.getId());
    }

    // ── Rutas asesor ──────────────────────────────────────────────────────────

    @GetMapping("/api/asesor/pedidos")
    public List<PedidoResponse> pedidosAsesor(@AuthenticationPrincipal User usuario) {
        return pedidoService.pedidosDeAsesor(usuario.getId());
    }

    @GetMapping("/api/asesor/pedidos/{id}")
    public PedidoResponse pedidoAsesor(@PathVariable Long id, @AuthenticationPrincipal User usuario) {
        return pedidoService.pedidoDeAsesor(id, usuario.getId());
    }

    @PatchMapping("/api/asesor/pedidos/{id}/estado")
    public PedidoResponse cambiarEstadoAsesor(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest req,
            @AuthenticationPrincipal User usuario
    ) {
        return pedidoService.cambiarEstado(id, usuario.getId(), req);
    }

    // ── Rutas admin ───────────────────────────────────────────────────────────

    @GetMapping("/api/admin/pedidos")
    public List<PedidoResponse> todosPedidos() {
        return pedidoService.todosLosPedidos();
    }

    @GetMapping("/api/admin/pedidos/{id}")
    public PedidoResponse pedidoAdmin(@PathVariable Long id) {
        return pedidoService.cualquierPedido(id);
    }

    @PatchMapping("/api/admin/pedidos/{id}/estado")
    public PedidoResponse cambiarEstadoAdmin(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest req,
            @AuthenticationPrincipal User usuario
    ) {
        return pedidoService.cambiarEstado(id, usuario.getId(), req);
    }

    @PatchMapping("/api/admin/pedidos/{id}/asesor")
    public PedidoResponse reasignarAsesor(
            @PathVariable Long id,
            @Valid @RequestBody ReasignarAsesorRequest req
    ) {
        return pedidoService.reasignarAsesor(id, req.asesorId());
    }
}
