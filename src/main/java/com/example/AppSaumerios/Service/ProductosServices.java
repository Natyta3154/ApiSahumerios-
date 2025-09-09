package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.dto.ProductoDTO;
import com.example.AppSaumerios.entity.*;
import com.example.AppSaumerios.repository.AtributoRepository;
import com.example.AppSaumerios.repository.CategoriaRepository;
import com.example.AppSaumerios.repository.DescuentoRepository;
import com.example.AppSaumerios.repository.FraganciaRepository;
import com.example.AppSaumerios.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductosServices {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private DescuentoRepository descuentoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private AtributoRepository atributoRepository;

    @Autowired
    private FraganciaRepository fraganciaRepository;

    // =========================
    // CRUD BÁSICO
    // =========================

    public List<Productos> listarTodos() {
        return productoRepository.findAll();
    }

    public Optional<Productos> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Optional<Productos> buscarPorNombre(String nombre) {
        return productoRepository.findByNombre(nombre);
    }

    public Productos guardarProductos(Productos productos) {
        validarStockYPrecio(productos);
        return productoRepository.save(productos);
    }

    public Productos actualizarProductos(Long id, Productos productoActualizado,
                                         BigDecimal porcentajeDescuento,
                                         LocalDate fechaInicioDescuento,
                                         LocalDate fechaFinDescuento) {

        Productos p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el producto con id " + id));

        validarStockYPrecio(productoActualizado);

        // ======================
        // Actualizar campos básicos
        // ======================
        if (productoActualizado.getNombre() != null) p.setNombre(productoActualizado.getNombre());
        if (productoActualizado.getDescripcion() != null) p.setDescripcion(productoActualizado.getDescripcion());
        if (productoActualizado.getPrecio() != null) p.setPrecio(productoActualizado.getPrecio());
        if (productoActualizado.getStock() >= 0) p.setStock(productoActualizado.getStock());
        if (productoActualizado.getActivo() != null) p.setActivo(productoActualizado.getActivo());
        if (productoActualizado.getImagenUrl() != null) p.setImagenUrl(productoActualizado.getImagenUrl());
        if (productoActualizado.getIdCategoria() != null) p.setIdCategoria(productoActualizado.getIdCategoria());
        if (productoActualizado.getPrecioMayorista() != null)
            p.setPrecioMayorista(productoActualizado.getPrecioMayorista());

        // ⚡ Corregido: mantener totalIngresado si no viene explícitamente
        if (productoActualizado.getTotalIngresado() != null) {
            p.setTotalIngresado(productoActualizado.getTotalIngresado());
        } else if (p.getTotalIngresado() == null) {
            p.setTotalIngresado(p.getStock()); // Inicializar con stock si estaba null
        }

        // ======================
        // Actualizar categoría
        // ======================
        if (productoActualizado.getCategoria() != null) {
            Categoria cat = categoriaRepository.findByNombre(productoActualizado.getCategoria().getNombre())
                    .orElseGet(() -> {
                        Categoria nueva = new Categoria();
                        nueva.setNombre(productoActualizado.getCategoria().getNombre());
                        return categoriaRepository.save(nueva);
                    });
            p.setCategoria(cat);
            p.setIdCategoria(cat.getId());
        }

        // ======================
        // Actualizar fragancias
        // ======================
        if (productoActualizado.getFragancias() != null && !productoActualizado.getFragancias().isEmpty()) {
            List<Fragancia> fraganciasActualizadas = productoActualizado.getFragancias().stream()
                    .map(f -> fraganciaRepository.findByNombre(f.getNombre())
                            .orElseGet(() -> fraganciaRepository.save(f)))
                    .collect(Collectors.toList());
            p.setFragancias(fraganciasActualizadas);
        }

        // ======================
        // Actualizar atributos
        // ======================
        if (productoActualizado.getProductoAtributos() != null && !productoActualizado.getProductoAtributos().isEmpty()) {
            p.getProductoAtributos().clear();
            productoActualizado.getProductoAtributos().forEach(pa ->
                    p.addAtributo(pa.getAtributo(), pa.getValor()));
        }

        // ======================
        // Actualizar descuento
        // ======================
        actualizarDescuento(p, porcentajeDescuento, fechaInicioDescuento, fechaFinDescuento);

        return productoRepository.save(p);
    }



    private void validarStockYPrecio(Productos producto) {
        BigDecimal zero = BigDecimal.ZERO;
        if (producto.getStock() < 0 || producto.getPrecio().compareTo(zero) < 0) {
            throw new IllegalArgumentException("El stock y el precio no pueden ser negativos");
        }
    }

    private void actualizarDescuento(Productos producto, BigDecimal porcentaje,
                                     LocalDate inicio, LocalDate fin) {
        Descuento descuentoActivo = producto.getDescuentoActivo();

        if (descuentoActivo != null) {
            if (porcentaje != null) descuentoActivo.setPorcentaje(porcentaje);
            if (inicio != null) descuentoActivo.setFechaInicio(inicio);
            if (fin != null) descuentoActivo.setFechaFin(fin);
            descuentoRepository.save(descuentoActivo);
        } else if (porcentaje != null) {
            Descuento nuevo = new Descuento();
            nuevo.setProducto(producto);
            nuevo.setPorcentaje(porcentaje);
            nuevo.setFechaInicio(inicio != null ? inicio : LocalDate.now());
            nuevo.setFechaFin(fin != null ? fin : LocalDate.now().plusDays(30));
            nuevo.setActivo(true);
            producto.getDescuentos().add(nuevo);
            descuentoRepository.save(nuevo);
        }
    }

    public void eliminarProductos(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar: producto no encontrado");
        }
        productoRepository.deleteById(id);
    }

    // =========================
    // DTO
    // =========================

    public List<ProductoDTO> listarTodosDTO() {
        return productoRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public ProductoDTO mapToDTO(Productos producto) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setPrecioMayorista(producto.getPrecioMayorista()); // 🔥 CORREGIDO
        dto.setTotalIngresado(producto.getTotalIngresado());
        dto.setStock(producto.getStock());
        dto.setImagenUrl(producto.getImagenUrl());
        dto.setActivo(producto.getActivo());

        // Categoría
        if (producto.getCategoria() != null) {
            dto.setCategoriaNombre(producto.getCategoria().getNombre());
        }

        // Descuento
        Descuento descuento = producto.getDescuentoActivo();
        if (descuento != null) {
            dto.setPorcentajeDescuento(descuento.getPorcentaje());
            dto.setFechaInicioDescuento(descuento.getFechaInicio());
            dto.setFechaFinDescuento(descuento.getFechaFin());

            BigDecimal rebaja = producto.getPrecio()
                    .multiply(descuento.getPorcentaje())
                    .divide(BigDecimal.valueOf(100));
            dto.setPrecioFinal(producto.getPrecio().subtract(rebaja));
        } else {
            dto.setPrecioFinal(producto.getPrecio());
        }

        // Atributos
        List<ProductoDTO.ProductoAtributoDTO> atributosDTO = producto.getProductoAtributos()
                .stream()
                .map(pa -> new ProductoDTO.ProductoAtributoDTO(pa.getAtributo().getNombre(), pa.getValor()))
                .toList();
        dto.setAtributos(atributosDTO);

        // Fragancias
        List<String> fraganciasDTO = producto.getFragancias()
                .stream()
                .map(Fragancia::getNombre)
                .toList();
        dto.setFragancias(fraganciasDTO);

        return dto;
    }

    // =========================
    // CREAR PRODUCTO COMPLETO DESDE DTO
    // =========================
    @Transactional
    public Productos crearProductoDesdeDTO(ProductoDTO dto) {
        Productos producto = new Productos();

        // ----------------------
        // Campos básicos
        // ----------------------
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());

        // Dentro de crearProductoDesdeDTO
        BigDecimal precioBase = dto.getPrecio() != null ? dto.getPrecio() : BigDecimal.ZERO;
        BigDecimal precioMayorista = dto.getPrecioMayorista() != null
                ? dto.getPrecioMayorista()
                : precioBase;

// Asegurar que nunca sea null
        if (precioMayorista == null) {
            precioMayorista = BigDecimal.ZERO;
        }

        producto.setPrecioMayorista(precioMayorista.setScale(2, RoundingMode.HALF_UP));


        int stock = dto.getStock() != null ? dto.getStock() : 0;
        producto.setStock(stock);

        int totalIngresado = dto.getTotalIngresado() != null ? dto.getTotalIngresado() : stock;
        producto.setTotalIngresado(totalIngresado);

        producto.setImagenUrl(dto.getImagenUrl());
        producto.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        producto.setFechaCreacion(LocalDateTime.now());

        // ----------------------
        // Categoría
        // ----------------------
        if (dto.getCategoriaNombre() != null && !dto.getCategoriaNombre().isBlank()) {
            Categoria cat = categoriaRepository.findByNombre(dto.getCategoriaNombre())
                    .orElseGet(() -> {
                        Categoria nueva = new Categoria();
                        nueva.setNombre(dto.getCategoriaNombre());
                        return categoriaRepository.save(nueva);
                    });
            producto.setCategoria(cat);
            producto.setIdCategoria(cat.getId());
        }

        // Guardar producto primero para generar ID
        producto = productoRepository.save(producto);

        // ----------------------
        // Fragancias
        // ----------------------
        if (dto.getFragancias() != null && !dto.getFragancias().isEmpty()) {
            List<Fragancia> fragancias = dto.getFragancias().stream()
                    .map(nombre -> fraganciaRepository.findByNombre(nombre)
                            .orElseGet(() -> fraganciaRepository.save(new Fragancia(nombre))))
                    .toList();
            producto.setFragancias(fragancias);
        }

        // ----------------------
        // Atributos
        // ----------------------
        if (dto.getAtributos() != null && !dto.getAtributos().isEmpty()) {
            Productos finalProducto = producto;
            Set<ProductoAtributo> productoAtributos = dto.getAtributos().stream()
                    .map(aDto -> {
                        Atributo atributo = atributoRepository.findByNombre(aDto.getNombre())
                                .orElseGet(() -> atributoRepository.save(new Atributo(aDto.getNombre())));
                        return new ProductoAtributo(finalProducto, atributo, aDto.getValor());
                    })
                    .collect(Collectors.toSet());
            producto.setProductoAtributos(productoAtributos);
        }

        // ----------------------
        // Descuento
        // ----------------------
        if (dto.getPorcentajeDescuento() != null) {
            Descuento descuento = new Descuento();
            descuento.setProducto(producto);
            descuento.setPorcentaje(dto.getPorcentajeDescuento());
            descuento.setFechaInicio(dto.getFechaInicioDescuento() != null ? dto.getFechaInicioDescuento() : LocalDate.now());
            descuento.setFechaFin(dto.getFechaFinDescuento() != null ? dto.getFechaFinDescuento() : LocalDate.now().plusDays(30));
            descuento.setActivo(true);
            producto.getDescuentos().add(descuento);
            descuentoRepository.save(descuento);
        }

        return productoRepository.save(producto);
    }
}