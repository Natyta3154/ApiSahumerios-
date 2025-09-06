package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.dto.ProductoDTO;
import com.example.AppSaumerios.entity.Atributo;
import com.example.AppSaumerios.entity.Descuento;
import com.example.AppSaumerios.entity.Fragancia;
import com.example.AppSaumerios.entity.ProductoAtributo;
import com.example.AppSaumerios.entity.Productos;
import com.example.AppSaumerios.repository.AtributoRepository;
import com.example.AppSaumerios.repository.CategoriaRepository;
import com.example.AppSaumerios.repository.DescuentoRepository;
import com.example.AppSaumerios.repository.FraganciaRepository;
import com.example.AppSaumerios.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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

        // Actualizar campos básicos
        if (productoActualizado.getNombre() != null) p.setNombre(productoActualizado.getNombre());
        if (productoActualizado.getDescripcion() != null) p.setDescripcion(productoActualizado.getDescripcion());
        if (productoActualizado.getPrecio() != null) p.setPrecio(productoActualizado.getPrecio());
        if (productoActualizado.getStock() >= 0) p.setStock(productoActualizado.getStock());
        if (productoActualizado.getActivo() != null) p.setActivo(productoActualizado.getActivo());
        if (productoActualizado.getImagenurl() != null) p.setImagenurl(productoActualizado.getImagenurl());
        if (productoActualizado.getIdCategoria() != null) p.setIdCategoria(productoActualizado.getIdCategoria());

        // ===== Actualizar descuento =====
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
        ProductoDTO dto = new ProductoDTO( );
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setPrecioMayorista(dto.getPrecioMayorista());
        dto.setStock(producto.getStock());
        dto.setImagenUrl(producto.getImagenurl());
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
        // 1️⃣ Crear producto básico
        Productos producto = new Productos();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock() != null ? dto.getStock() : 0);
        // Inicializar totalIngresado igual al stock inicial
        producto.setTotalIngresado(dto.getStock() != null ? dto.getStock() : 0);
        producto.setImagenurl(dto.getImagenUrl());
        producto.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        // Asignar categoría usando nombre
        if (dto.getCategoriaNombre() != null) {
            final Productos prod = producto;
            categoriaRepository.findByNombre(dto.getCategoriaNombre())
                    .ifPresent(categoria -> prod.setIdCategoria(categoria.getId()));
        }

        if (dto.getPrecioMayorista() != null) {
            producto.setPrecioMayorista(dto.getPrecioMayorista());
        }

        // Guardar primero para generar ID
        producto = productoRepository.save(producto);

        // 2️⃣ Actualizar precios de la categoría si corresponde
        if (producto.getIdCategoria() != null && dto.getPrecio() != null && dto.getPrecioMayorista() != null) {
            productoRepository.actualizarPreciosPorCategoria(
                    producto.getIdCategoria(),
                    dto.getPrecio(),
                    dto.getPrecioMayorista()
            );
        }

        // 3️⃣ Asignar fragancias
        if (dto.getFragancias() != null && !dto.getFragancias().isEmpty()) {
            List<Fragancia> fragancias = dto.getFragancias().stream()
                    .map(nombre -> fraganciaRepository.findByNombre(nombre)
                            .orElseGet(() -> fraganciaRepository.save(new Fragancia(nombre))))
                    .toList();
            producto.setFragancias(fragancias);
        }

        // 4️⃣ Asignar atributos
        if (dto.getAtributos() != null && !dto.getAtributos().isEmpty()) {
            Productos finalProducto = producto;
            Set<ProductoAtributo> productoAtributos = dto.getAtributos().stream()
                    .map(aDto -> {
                        Atributo atributo = atributoRepository.findByNombre(aDto.getNombre())
                                .orElseGet(() -> atributoRepository.save(new Atributo(aDto.getNombre())));
                        return new ProductoAtributo(finalProducto, atributo, aDto.getValor());
                    }).collect(Collectors.toSet());
            producto.setProductoAtributos(productoAtributos);
        }

        return productoRepository.save(producto);
    }


}
