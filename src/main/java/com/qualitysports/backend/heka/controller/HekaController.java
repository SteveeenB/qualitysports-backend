package com.qualitysports.backend.heka.controller;

import com.qualitysports.backend.heka.config.HekaProperties;
import com.qualitysports.backend.heka.dto.*;
import com.qualitysports.backend.heka.service.HekaShippingService;
import com.qualitysports.backend.pedido.dto.PedidoResponse;
import com.qualitysports.backend.pedido.entity.ModalidadEntrega;
import com.qualitysports.backend.pedido.entity.Pedido;
import com.qualitysports.backend.pedido.repository.PedidoRepository;
import com.qualitysports.backend.pedido.service.PedidoService;
import com.qualitysports.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/heka")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ASESOR_VENTAS','ADMINISTRADOR')")
public class HekaController {

    private final HekaShippingService hekaService;
    private final PedidoRepository pedidoRepository;
    private final PedidoService pedidoService;

    @GetMapping("/ciudades")
    public List<HekaCityResponse.CityItem> buscarCiudad(@RequestParam String label) {
        HekaCityResponse resp = hekaService.searchCity(label);
        return resp != null && resp.response() != null ? resp.response() : List.of();
    }

    @GetMapping("/defaults")
    public Map<String, Object> getDefaults() {
        HekaProperties p = hekaService.getDefaults();
        return Map.of(
                "weight", p.getDefaultWeight(),
                "height", p.getDefaultHeight(),
                "longDim", p.getDefaultLong(),
                "width",  p.getDefaultWidth()
        );
    }

    @PostMapping("/cotizar")
    public List<HekaQuoteResponse.CarrierQuote> cotizar(@RequestBody HekaQuoteRequest req) {
        HekaQuoteResponse resp = hekaService.quote(req);
        return resp != null && resp.response() != null ? resp.response() : List.of();
    }

    @PostMapping("/guia/{pedidoId}")
    public PedidoResponse generarGuia(
            @PathVariable Long pedidoId,
            @RequestBody HekaCreateGuideRequest req,
            @AuthenticationPrincipal User usuario
    ) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

        if (pedido.getModalidadEntrega() != ModalidadEntrega.DOMICILIO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se generan guías para pedidos con modalidad DOMICILIO");
        }

        String producto = pedido.getDetalles().stream()
                .map(d -> d.getProducto().getNombre())
                .limit(3)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Calzado deportivo");

        HekaCreateGuideRequest fullReq = new HekaCreateGuideRequest(
                req.distributorId(),
                req.cityDestination(),
                req.declaredValue() != null ? req.declaredValue() : pedido.getTotalNeto(),
                req.total(),
                req.weight(),
                req.height(),
                req.longDim(),
                req.width(),
                req.collectionValue() != null ? req.collectionValue() : pedido.getTotalNeto(),
                producto,
                req.note()
        );

        HekaCreateGuideResponse hekaResp = hekaService.createGuide(
                fullReq,
                pedido.getCompradorNombre(),
                pedido.getCompradorApellido(),
                pedido.getDireccionEnvio(),
                pedido.getCompradorTelefono(),
                pedido.getBarrio(),
                pedido.getCompradorCedula()
        );

        if (hekaResp == null || hekaResp.response() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Respuesta inválida de HekaEntrega");
        }

        return pedidoService.registrarGuia(
                pedidoId,
                usuario.getId(),
                hekaResp.response().guideNumber(),
                req.distributorId(),
                hekaResp.response().shipmentId(),
                req.total() != null ? req.total() : BigDecimal.ZERO
        );
    }
}
