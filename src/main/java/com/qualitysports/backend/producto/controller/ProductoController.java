package com.qualitysports.backend.producto.controller;

import com.qualitysports.backend.producto.dto.*;
import com.qualitysports.backend.producto.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // ── Rutas públicas ────────────────────────────────────────────────────────

    @GetMapping("/api/productos")
    public Page<ProductoDTO> listar(
            @RequestParam(required = false) Long modeloId,
            @PageableDefault(size = 20) Pageable pageable) {
        if (modeloId != null) return productoService.listarActivosPorModelo(modeloId, pageable);
        return productoService.listarActivos(pageable);
    }

    @GetMapping("/api/productos/search")
    public List<ProductoDTO> buscar(@RequestParam String q) {
        return productoService.buscar(q);
    }

    @GetMapping("/api/productos/{id}")
    public ProductoDTO detalle(@PathVariable Long id) {
        return productoService.obtenerPorId(id);
    }

    @GetMapping("/api/categorias")
    public List<CategoriaDTO> categorias() {
        return productoService.listarCategorias();
    }

    @GetMapping("/api/modelos")
    public List<ModeloDTO> modelos() {
        return productoService.listarModelos();
    }

    // ── Rutas admin — productos ───────────────────────────────────────────────

    @GetMapping("/api/admin/productos")
    public Page<ProductoDTO> listarAdmin(@PageableDefault(size = 20) Pageable pageable) {
        return productoService.listarTodos(pageable);
    }

    @GetMapping("/api/admin/productos/{id}")
    public ProductoDTO detalleAdmin(@PathVariable Long id) {
        return productoService.obtenerPorIdAdmin(id);
    }

    @PostMapping("/api/admin/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoDTO crear(@Valid @RequestBody CreateProductoRequest req) {
        return productoService.crear(req);
    }

    @PutMapping("/api/admin/productos/{id}")
    public ProductoDTO actualizar(@PathVariable Long id, @Valid @RequestBody UpdateProductoRequest req) {
        return productoService.actualizar(id, req);
    }

    @PostMapping("/api/admin/productos/{id}/imagen")
    public ProductoDTO subirImagen(@PathVariable Long id,
                                   @RequestParam("file") MultipartFile file) {
        return productoService.subirImagen(id, file);
    }

    @PatchMapping("/api/admin/productos/{id}/estado")
    public ProductoDTO cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean activo = body.getOrDefault("activo", true);
        return productoService.cambiarEstado(id, activo);
    }

    // ── Rutas admin — modelos ─────────────────────────────────────────────────

    @GetMapping("/api/admin/modelos")
    public List<ModeloDTO> listarModelosAdmin() {
        return productoService.listarModelos();
    }

    @PostMapping("/api/admin/modelos")
    @ResponseStatus(HttpStatus.CREATED)
    public ModeloDTO crearModelo(@RequestBody Map<String, String> body) {
        return productoService.crearModelo(body.get("nombre"));
    }

    @PutMapping("/api/admin/modelos/{id}")
    public ModeloDTO actualizarModelo(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return productoService.actualizarModelo(id, body.get("nombre"));
    }

    @DeleteMapping("/api/admin/modelos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarModelo(@PathVariable Long id) {
        productoService.eliminarModelo(id);
    }
}
