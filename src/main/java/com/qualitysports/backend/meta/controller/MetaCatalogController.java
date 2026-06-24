package com.qualitysports.backend.meta.controller;

import com.qualitysports.backend.meta.config.MetaProperties;
import com.qualitysports.backend.meta.dto.MetaCatalogProductoDTO;
import com.qualitysports.backend.producto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MetaCatalogController {

    private final ProductoRepository productoRepository;
    private final MetaProperties props;

    private static final String SITE_BASE_URL = "https://qualitysports-iw7tj.ondigitalocean.app";
    private static final String BRAND         = "Quality Sports";

    @GetMapping("/api/meta/catalog")
    public ResponseEntity<List<MetaCatalogProductoDTO>> catalog(
            @RequestHeader(value = "X-Catalog-Token", required = false) String headerToken) {

        // Si hay token configurado, verificarlo — Meta lo envía como header en Commerce Manager
        if (!props.getCatalogToken().isBlank()) {
            if (!props.getCatalogToken().equals(headerToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        List<MetaCatalogProductoDTO> feed = productoRepository.findAll().stream()
                .filter(p -> p.isActivo())
                .map(p -> {
                    MetaCatalogProductoDTO dto = new MetaCatalogProductoDTO();
                    dto.setId(String.valueOf(p.getId()));
                    dto.setTitle(p.getNombre());
                    dto.setDescription(
                            p.getDescripcion() != null && !p.getDescripcion().isBlank()
                                    ? p.getDescripcion()
                                    : p.getNombre()
                    );
                    dto.setAvailability("in stock");
                    dto.setCondition("new");
                    // Formato requerido por Meta: "95000 COP"
                    dto.setPrice(p.getPrecioBase().toPlainString() + " COP");
                    dto.setCurrency("COP");
                    dto.setLink(SITE_BASE_URL + "/producto/" + p.getId());
                    dto.setImageLink(p.getImagenUrl() != null ? p.getImagenUrl() : "");
                    dto.setBrand(BRAND);
                    return dto;
                })
                .toList();

        return ResponseEntity.ok()
                .header("Cache-Control", "public, max-age=3600")
                .body(feed);
    }
}
