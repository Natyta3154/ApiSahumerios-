package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.dto.ProductoAtributoDTO;
import com.example.AppSaumerios.dto.ProductoDTO;
import com.example.AppSaumerios.dto.ProductoResumenDTO;
import com.example.AppSaumerios.dto.ProductoUpdateDTO;
import com.example.AppSaumerios.entity.*;
import com.example.AppSaumerios.repository.*;
import com.example.AppSaumerios.util.ProductoMapper;
import org.springframework.cache.annotation.Cacheable;
//import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

//import java.awt.print.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private AtributoRepository atributoRepository;

    @Autowired
    private FraganciaRepository fraganciaRepository;

    @Autowired
    private OfertaService ofertaService;

    // =========================
    // CRUD Básico
    // =========================
    public List<Productos> listarTodos() {
        return productoRepository.findAll();
    }

    public Optional<Productos> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Productos guardarProductos(Productos productos) {
        validarStockYPrecio(productos);
        return productoRepository.save(productos);
    }

    // =========================
    // Productos destacados y relacionados
    // =========================
    public List<ProductoResumenDTO> listarRelacionados(Long categoriaId, Long excludeId) {
        List<Productos> productos = productoRepository
                .findTop4ByActivoTrueAndCategoriaIdOrderByPrecioDesc(categoriaId);
        if (excludeId != null) {
            productos = productos.stream()
                    .filter(p -> !p.getId().equals(excludeId))
                    .toList();
        }
        return productos.stream()
                .map(p -> new ProductoResumenDTO(
                        p.getId(), p.getNombre(), p.getPrecio(), p.getImagenUrl(),
                        p.getCategoria().getNombre(), p.getDestacado(), p.getDescripcion()))
                .toList();
    }

    @Cacheable("productosDestacados")
    @Transactional(readOnly = true)
    public List<Productos> obtenerProductosDestacados(Long categoriaId) {
        if (categoriaId != null) {
            return productoRepository.findTop4ByActivoTrueAndCategoriaIdOrderByPrecioDesc(categoriaId);
        } else {
            return productoRepository.findTop4ByActivoTrueOrderByPrecioDesc();
        }
    }

    @Cacheable("productosDestacadosDTO")
    @Transactional(readOnly = true)
    public List<ProductoDTO> obtenerProductosDestacadosDTO() {
        List<Productos> productos = productoRepository.findDestacadosConRelaciones();
        return productos.stream()
                .limit(4)
                .map(p -> ProductoMapper.toDTO(p, null))
                .peek(dto -> dto.setDestacado(true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductoResumenDTO> listarResumen(Pageable pageable) {
        return productoRepository.listarResumen(pageable);
    }

    // =========================
    // Actualizar campos básicos del producto
    // =========================
    @Transactional
    public Productos actualizarCamposBasicos(Long id, ProductoUpdateDTO dto) {
        Productos producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el producto con id " + id));

        if (dto.getNombre() != null) producto.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) producto.setDescripcion(dto.getDescripcion());
        if (dto.getPrecio() != null) producto.setPrecio(dto.getPrecio());
        if (dto.getPrecioMayorista() != null) producto.setPrecioMayorista(dto.getPrecioMayorista());
        if (dto.getStock() != null) producto.setStock(dto.getStock());
        if (dto.getTotalIngresado() != null) producto.setTotalIngresado(dto.getTotalIngresado());
        if (dto.getActivo() != null) producto.setActivo(dto.getActivo());
        if (dto.getImagenUrl() != null) producto.setImagenUrl(dto.getImagenUrl());

        validarStockYPrecio(producto);
        return productoRepository.save(producto);
    }

    // =========================
    // Actualizar categoría
    // =========================
    @Transactional
    public Productos actualizarCategoria(Long id, String nombreCategoria) {
        Productos producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el producto con id " + id));

        if (nombreCategoria != null && !nombreCategoria.isBlank()) {
            Categoria categoria = categoriaRepository.findByNombre(nombreCategoria)
                    .orElseGet(() -> {
                        Categoria nueva = new Categoria();
                        nueva.setNombre(nombreCategoria);
                        return categoriaRepository.save(nueva);
                    });
            producto.setCategoria(categoria);
        }

        return productoRepository.save(producto);
    }

    // =========================
    // Actualizar fragancias
    // =========================
    @Transactional
    public Productos actualizarFragancias(Long id, List<String> fraganciasNombres) {
        Productos producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el producto con id " + id));

        if (fraganciasNombres != null && !fraganciasNombres.isEmpty()) {
            List<Fragancia> fragancias = fraganciasNombres.stream()
                    .map(nombre -> fraganciaRepository.findByNombre(nombre)
                            .orElseGet(() -> {
                                Fragancia f = new Fragancia();
                                f.setNombre(nombre);
                                return fraganciaRepository.save(f);
                            }))
                    .toList();
            producto.setFragancias(fragancias);
        }

        return productoRepository.save(producto);
    }

    // =========================
    // Actualizar atributos
    // =========================
    @Transactional
    public Productos actualizarAtributos(Long id, List<ProductoDTO.ProductoAtributoDTO> atributosDTO) {
        Productos producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el producto con id " + id));

        if (atributosDTO != null && !atributosDTO.isEmpty()) {
            List<ProductoAtributo> productoAtributos = atributosDTO.stream()
                    .map(aDto -> {
                        Atributo atributo = atributoRepository.findByNombre(aDto.getNombre())
                                .orElseGet(() -> {
                                    Atributo a = new Atributo();
                                    a.setNombre(aDto.getNombre());
                                    return atributoRepository.save(a);
                                });
                        return new ProductoAtributo(producto, atributo, aDto.getValor());
                    })
                    .toList();
            producto.setProductoAtributos(productoAtributos);
        }

        return productoRepository.save(producto);
    }

    // =========================
    // Eliminar producto
    // =========================
    @Transactional
    public String eliminarProductos(Long id) {
        Productos producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        int cantidadOfertas = producto.getOfertas() != null ? producto.getOfertas().size() : 0;
        int cantidadPedidos = producto.getDetallePedidos() != null ? producto.getDetallePedidos().size() : 0;

        if (cantidadPedidos > 0) {
            throw new RuntimeException("No se puede eliminar el producto: está asociado a "
                    + cantidadPedidos + " pedido(s) existente(s)");
        }

        productoRepository.delete(producto);

        String mensaje = "Producto eliminado correctamente";
        if (cantidadOfertas > 0) {
            mensaje += " junto con " + cantidadOfertas + " oferta(s) asociada(s)";
        }

        return mensaje;
    }

    // =========================
    // Vender producto
    // =========================
    @Transactional
    public Productos venderProducto(Long productoId, int cantidadVendida) {
        Productos producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (cantidadVendida <= 0)
            throw new IllegalArgumentException("La cantidad vendida debe ser mayor a cero");

        if (producto.getStock() < cantidadVendida)
            throw new IllegalArgumentException("No hay suficiente stock");

        producto.setStock(producto.getStock() - cantidadVendida);
        return productoRepository.save(producto);
    }

    private void validarStockYPrecio(Productos producto) {
        BigDecimal zero = BigDecimal.ZERO;
        if (producto.getStock() < 0 || producto.getPrecio().compareTo(zero) < 0) {
            throw new IllegalArgumentException("El stock y el precio no pueden ser negativos");
        }
    }
}
