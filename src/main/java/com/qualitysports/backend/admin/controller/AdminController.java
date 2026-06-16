package com.qualitysports.backend.admin.controller;

import com.qualitysports.backend.admin.dto.*;
import com.qualitysports.backend.admin.service.AdminService;
import com.qualitysports.backend.pedido.dto.PedidoResponse;
import com.qualitysports.backend.pedido.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
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

    @GetMapping("/asesores")
    public List<AsesorDTO> listarAsesores() {
        return adminService.listarAsesores();
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
