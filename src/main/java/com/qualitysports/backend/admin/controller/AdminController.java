package com.qualitysports.backend.admin.controller;

import com.qualitysports.backend.admin.dto.*;
import com.qualitysports.backend.admin.service.AdminService;
import com.qualitysports.backend.pedido.dto.PedidoResponse;
import com.qualitysports.backend.pedido.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminController {

    private final AdminService adminService;
    private final PedidoService pedidoService;

    @GetMapping("/clientes")
    public List<ClienteAdminDTO> listarClientes() {
        return adminService.listarClientes();
    }

    @GetMapping("/clientes/{id}/historial")
    public List<PedidoResponse> historialCliente(@PathVariable Long id) {
        return pedidoService.pedidosDeCliente(id);
    }

    @PostMapping("/clientes")
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteAdminDTO crearCliente(@RequestBody CrearClienteRequest req) {
        return adminService.crearCliente(req);
    }

    @PutMapping("/clientes/{id}")
    public ClienteAdminDTO actualizarCliente(@PathVariable Long id, @RequestBody ActualizarClienteRequest req) {
        return adminService.actualizarCliente(id, req);
    }

    @DeleteMapping("/clientes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivarCliente(@PathVariable Long id) {
        adminService.desactivarCliente(id);
    }

    @GetMapping("/asesores")
    public List<AsesorDTO> listarAsesores() {
        return adminService.listarAsesores();
    }

    @PostMapping("/asesores")
    @ResponseStatus(HttpStatus.CREATED)
    public AsesorDTO crearAsesor(@RequestBody CrearAsesorRequest req) {
        return adminService.crearAsesor(req);
    }

    @PutMapping("/asesores/{id}")
    public AsesorDTO actualizarAsesor(@PathVariable Long id, @RequestBody ActualizarAsesorRequest req) {
        return adminService.actualizarAsesor(id, req);
    }

    @DeleteMapping("/asesores/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivarAsesor(@PathVariable Long id) {
        adminService.desactivarAsesor(id);
    }

    @GetMapping("/kpis")
    public KpiResponse kpis() {
        return adminService.obtenerKpis();
    }

    @GetMapping("/descuentos")
    public List<ReglaPaqueteDTO> listarDescuentos() {
        return adminService.listarReglas();
    }

    @PostMapping("/descuentos")
    @ResponseStatus(HttpStatus.CREATED)
    public ReglaPaqueteDTO crearDescuento(@Valid @RequestBody ReglaPaqueteDTO dto) {
        return adminService.crearRegla(dto);
    }

    @PutMapping("/descuentos/{id}")
    public ReglaPaqueteDTO actualizarDescuento(@PathVariable Long id, @Valid @RequestBody ReglaPaqueteDTO dto) {
        return adminService.actualizarRegla(id, dto);
    }

    @DeleteMapping("/descuentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarDescuento(@PathVariable Long id) {
        adminService.eliminarRegla(id);
    }
}
