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


    //los rrrelacionado para la pageDetalle
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


    // =========================
    // Productos destacados
    // =========================
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

    // =========================
    // Resumen de productos
    // =========================
    @Transactional(readOnly = true)
    public Page<ProductoResumenDTO> listarResumen(Pageable pageable) {
        return productoRepository.listarResumen(pageable);
    }

    // =========================
    // Actualizar productos
    // =========================
    @Transactional
    public Productos actualizarProductos(Long id, ProductoUpdateDTO dto) {
        Productos producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el producto con id " + id));

        // Actualizar campos básicos directamente desde el DTO
        ProductoMapper.updateEntityFromDTO(dtoToProductoDTO(dto), producto);

        // Categoría
        if (dto.getCategoriaNombre() != null && !dto.getCategoriaNombre().isBlank()) {
            Categoria cat = categoriaRepository.findByNombre(dto.getCategoriaNombre())
                    .orElseGet(() -> {
                        Categoria nueva = new Categoria();  // constructor vacío
                        nueva.setNombre(dto.getCategoriaNombre());
                        return categoriaRepository.save(nueva);
                    });
            producto.setCategoria(cat);
        }

        // Fragancias
        if (dto.getFragancias() != null && !dto.getFragancias().isEmpty()) {
            List<Fragancia> fragancias = dto.getFragancias().stream()
                    .map(nombre -> fraganciaRepository.findByNombre(nombre)
                            .orElseGet(() -> {
                                Fragancia f = new Fragancia();
                                f.setNombre(nombre);
                                return fraganciaRepository.save(f);
                            }))
                    .toList();
            producto.setFragancias(fragancias);
        }

        // Atributos
        if (dto.getAtributos() != null && !dto.getAtributos().isEmpty()) {
            List<ProductoAtributo> productoAtributos = dto.getAtributos().stream()
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

        validarStockYPrecio(producto);
        return productoRepository.save(producto);
    }

    // Método auxiliar para mapear ProductoUpdateDTO -> ProductoDTO
    private ProductoDTO dtoToProductoDTO(ProductoUpdateDTO dto) {
        ProductoDTO tempDTO = new ProductoDTO();
        tempDTO.setNombre(dto.getNombre());
        tempDTO.setDescripcion(dto.getDescripcion());
        tempDTO.setPrecio(dto.getPrecio());
        tempDTO.setPrecioMayorista(dto.getPrecioMayorista());
        tempDTO.setStock(dto.getStock());
        tempDTO.setTotalIngresado(dto.getTotalIngresado());
        tempDTO.setImagenUrl(dto.getImagenUrl());
        tempDTO.setActivo(dto.getActivo());
        tempDTO.setCategoriaNombre(dto.getCategoriaNombre());
        tempDTO.setFragancias(dto.getFragancias());
        tempDTO.setAtributos(
                dto.getAtributos().stream()
                        .map(a -> new ProductoDTO.ProductoAtributoDTO(a.getNombre(), a.getValor()))
                        .collect(Collectors.toList())
        );
        return tempDTO;
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
