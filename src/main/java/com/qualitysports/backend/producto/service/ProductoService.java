package com.qualitysports.backend.producto.service;

import com.qualitysports.backend.config.SupabaseStorageService;
import com.qualitysports.backend.producto.dto.*;
import com.qualitysports.backend.producto.entity.Categoria;
import com.qualitysports.backend.producto.entity.Modelo;
import com.qualitysports.backend.producto.entity.Producto;
import com.qualitysports.backend.producto.repository.CategoriaRepository;
import com.qualitysports.backend.producto.repository.ModeloRepository;
import com.qualitysports.backend.producto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ModeloRepository modeloRepository;
    private final SupabaseStorageService supabaseStorageService;

    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarActivos(Pageable pageable) {
        return productoRepository.findByActivoTrue(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarActivosPorModelo(Long modeloId, Pageable pageable) {
        return productoRepository.findByModelo_IdAndActivoTrue(modeloId, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarTodos(Pageable pageable) {
        return productoRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarTodosConFiltros(String buscar, Long modeloId, Pageable pageable) {
        String q = (buscar != null && !buscar.isBlank()) ? buscar : null;
        return productoRepository.findWithFilters(q, modeloId, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Long id) {
        return productoRepository.findByIdAndActivoTrue(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorIdAdmin(Long id) {
        return productoRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> buscar(String q) {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(q)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .map(c -> new CategoriaDTO(c.getId(), c.getNombreCategoria()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ModeloDTO> listarModelos() {
        return modeloRepository.findAll().stream()
                .map(m -> {
                    String img = productoRepository
                            .findFirstByModelo_IdAndActivoTrueAndImagenUrlIsNotNull(m.getId())
                            .map(Producto::getImagenUrl)
                            .orElse(null);
                    return new ModeloDTO(m.getId(), m.getNombre(), img);
                })
                .toList();
    }

    @Transactional
    public ModeloDTO crearModelo(String nombre) {
        if (modeloRepository.existsByNombre(nombre))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un modelo con ese nombre");
        Modelo m = modeloRepository.save(Modelo.builder().nombre(nombre).build());
        return new ModeloDTO(m.getId(), m.getNombre(), null);
    }

    @Transactional
    public ModeloDTO actualizarModelo(Long id, String nombre) {
        Modelo m = modeloRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo no encontrado"));
        m.setNombre(nombre);
        return new ModeloDTO(m.getId(), m.getNombre(), null);
    }

    @Transactional
    public void eliminarModelo(Long id) {
        if (!modeloRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo no encontrado");
        if (productoRepository.existsByModelo_Id(id))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede eliminar: hay productos asignados a este modelo");
        modeloRepository.deleteById(id);
    }

    @Transactional
    public ProductoDTO crear(CreateProductoRequest req) {
        Producto producto = Producto.builder()
                .nombre(req.nombre())
                .descripcion(req.descripcion())
                .precioBase(req.precioBase())
                .imagenUrl(req.imagenUrl())
                .categoria(resolverCategoria(req.categoriaId()))
                .modelo(resolverModelo(req.modeloId()))
                .tallasDisponibles(req.tallasDisponibles() != null ? new HashSet<>(req.tallasDisponibles()) : new HashSet<>())
                .build();
        return toDTO(productoRepository.save(producto));
    }

    @Transactional
    public ProductoDTO actualizar(Long id, UpdateProductoRequest req) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        if (req.nombre() != null) producto.setNombre(req.nombre());
        if (req.descripcion() != null) producto.setDescripcion(req.descripcion());
        if (req.precioBase() != null) producto.setPrecioBase(req.precioBase());
        if (req.imagenUrl() != null) producto.setImagenUrl(req.imagenUrl());
        if (req.categoriaId() != null) producto.setCategoria(resolverCategoria(req.categoriaId()));
        if (req.modeloId() != null) producto.setModelo(resolverModelo(req.modeloId()));
        if (req.tallasDisponibles() != null) producto.setTallasDisponibles(new HashSet<>(req.tallasDisponibles()));

        return toDTO(productoRepository.save(producto));
    }

    @Transactional
    public ProductoDTO subirImagen(Long id, MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Solo se permiten imágenes (jpeg, png, webp, gif)");
        }
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo está vacío");
        }

        String extension = switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/webp" -> "webp";
            case "image/gif"  -> "gif";
            default           -> "jpg";
        };
        String fileName = UUID.randomUUID() + "." + extension;

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo");
        }

        String url = supabaseStorageService.upload(fileName, bytes, contentType);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        producto.setImagenUrl(url);
        return toDTO(productoRepository.save(producto));
    }

    @Transactional
    public ProductoDTO cambiarEstado(Long id, boolean activo) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        producto.setActivo(activo);
        return toDTO(productoRepository.save(producto));
    }

    private Categoria resolverCategoria(Long categoriaId) {
        if (categoriaId == null) return null;
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
    }

    private Modelo resolverModelo(Long modeloId) {
        if (modeloId == null) return null;
        return modeloRepository.findById(modeloId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo no encontrado"));
    }

    public ProductoDTO toDTO(Producto p) {
        CategoriaDTO catDTO = p.getCategoria() != null
                ? new CategoriaDTO(p.getCategoria().getId(), p.getCategoria().getNombreCategoria())
                : null;
        ModeloDTO modDTO = p.getModelo() != null
                ? new ModeloDTO(p.getModelo().getId(), p.getModelo().getNombre(), null)
                : null;
        return new ProductoDTO(p.getId(), p.getNombre(), p.getDescripcion(),
                p.getPrecioBase(), p.getImagenUrl(), catDTO, modDTO, p.isActivo(), p.getTallasDisponibles());
    }
}
