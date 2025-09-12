package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.dto.OfertaDTO;
import com.example.AppSaumerios.dto.ProductoDTO;
import com.example.AppSaumerios.dto.ProductoOfertaDTO;
import com.example.AppSaumerios.dto.ProductoUpdateDTO;
import com.example.AppSaumerios.entity.*;
import com.example.AppSaumerios.repository.AtributoRepository;
import com.example.AppSaumerios.repository.CategoriaRepository;
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
    private OfertaService ofertaService; // <-- inyectamos OfertaService

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

    public Productos actualizarProductos(Long id, ProductoUpdateDTO dto) {
        Productos p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el producto con id " + id));

        Productos productoActualizado = dto.toProductos();
        validarStockYPrecio(productoActualizado);

        if (productoActualizado.getNombre() != null) p.setNombre(productoActualizado.getNombre());
        if (productoActualizado.getDescripcion() != null) p.setDescripcion(productoActualizado.getDescripcion());
        if (productoActualizado.getPrecio() != null) p.setPrecio(productoActualizado.getPrecio());
        if (productoActualizado.getStock() >= 0) p.setStock(productoActualizado.getStock());
        if (productoActualizado.getActivo() != null) p.setActivo(productoActualizado.getActivo());
        if (productoActualizado.getImagenUrl() != null) p.setImagenUrl(productoActualizado.getImagenUrl());
        if (productoActualizado.getIdCategoria() != null) p.setIdCategoria(productoActualizado.getIdCategoria());
        if (productoActualizado.getPrecioMayorista() != null)
            p.setPrecioMayorista(productoActualizado.getPrecioMayorista());

        if (productoActualizado.getTotalIngresado() != null) {
            p.setTotalIngresado(productoActualizado.getTotalIngresado());
        } else if (p.getTotalIngresado() == null) {
            p.setTotalIngresado(p.getStock());
        }

        if (dto.getCategoriaNombre() != null && !dto.getCategoriaNombre().isBlank()) {
            Categoria cat = categoriaRepository.findByNombre(dto.getCategoriaNombre())
                    .orElseGet(() -> {
                        Categoria nueva = new Categoria();
                        nueva.setNombre(dto.getCategoriaNombre());
                        return categoriaRepository.save(nueva);
                    });
            p.setCategoria(cat);
            p.setIdCategoria(cat.getId());
        }

        if (dto.getFragancias() != null && !dto.getFragancias().isEmpty()) {
            List<Fragancia> fraganciasActualizadas = dto.getFragancias().stream()
                    .map(nombre -> fraganciaRepository.findByNombre(nombre)
                            .orElseGet(() -> fraganciaRepository.save(new Fragancia(nombre))))
                    .collect(Collectors.toList());
            p.setFragancias(fraganciasActualizadas);
        }

        if (dto.getAtributos() != null && !dto.getAtributos().isEmpty()) {
            p.getProductoAtributos().clear();
            for (ProductoUpdateDTO.ProductoAtributoDTO attrDTO : dto.getAtributos()) {
                Atributo atributo = atributoRepository.findByNombre(attrDTO.getNombre())
                        .orElseGet(() -> atributoRepository.save(new Atributo(attrDTO.getNombre())));
                p.addAtributo(atributo, attrDTO.getValor());
            }
        }

        return productoRepository.save(p);
    }

    private void validarStockYPrecio(Productos producto) {
        BigDecimal zero = BigDecimal.ZERO;
        if (producto.getStock() < 0 || producto.getPrecio().compareTo(zero) < 0) {
            throw new IllegalArgumentException("El stock y el precio no pueden ser negativos");
        }
    }

    public void eliminarProductos(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar: producto no encontrado");
        }
        productoRepository.deleteById(id);
    }

    // =========================
// LISTAR TODOS LOS PRODUCTOS CON OFERTAS
// =========================
    public List<ProductoDTO> listarTodosDTO() {
        // 1. Obtener todos los productos
        List<Productos> productos = productoRepository.findAll();

        // 2. Obtener todas las ofertas activas
        List<ProductoOfertaDTO> todasLasOfertas = ofertaService.listarOfertasConPrecioFinal(); // devuelve List<ProductoOfertaDTO>

        // 3. Mapear cada producto a DTO, pasando la lista de ofertas
        return productos.stream()
                .map(producto -> mapToDTO(producto, todasLasOfertas))
                .toList();
    }

    // ==========================
// Mapear Producto a ProductoDTO
// ==========================
    public ProductoDTO mapToDTO(Productos producto, List<ProductoOfertaDTO> todasLasOfertas) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setPrecioMayorista(producto.getPrecioMayorista());
        dto.setStock(producto.getStock());
        dto.setTotalIngresado(producto.getTotalIngresado());
        dto.setImagenUrl(producto.getImagenUrl());
        dto.setActivo(producto.getActivo());
        dto.setCategoriaNombre(producto.getCategoria() != null ? producto.getCategoria().getNombre() : null);

        // ======================
        // Forzar carga de relaciones
        // ======================
        producto.getProductoAtributos().size(); // fuerza carga de atributos
        producto.getFragancias().size();        // fuerza carga de fragancias


        // atributos
        List<ProductoDTO.ProductoAtributoDTO> atributosDTO = producto.getProductoAtributos().stream()
                .map(pa -> new ProductoDTO.ProductoAtributoDTO(pa.getAtributo().getNombre(), pa.getValor()))
                .toList();
        dto.setAtributos(atributosDTO);

        // fragancias
        List<String> fraganciasDTO = producto.getFragancias().stream()
                .map(Fragancia::getNombre)
                .toList();
        dto.setFragancias(fraganciasDTO);

        // ofertas: solo la primera activa del producto
        List<ProductoDTO.OfertaSimpleDTO> ofertasFiltradas = todasLasOfertas.stream()
                .filter(of -> of.getId().equals(producto.getId())) // solo ofertas de este producto
                .findFirst() // tomamos la primera
                .map(of -> {
                    ProductoDTO.OfertaSimpleDTO oDto = new ProductoDTO.OfertaSimpleDTO();
                    oDto.setIdOferta(of.getId());
                    oDto.setPrecio(of.getPrecioConDescuento());
                    oDto.setValorDescuento(of.getPrecioOriginal().subtract(of.getPrecioConDescuento()));
                    oDto.setTipoDescuento("PORCENTAJE"); // ajustar según lógica real
                    oDto.setEstado(true);
                    oDto.setFechaInicio(of.getFechaInicio()); // <--- ahora sí asigna
                    oDto.setFechaFin(of.getFechaFin());
                    return oDto;
                })
                .stream()
                .toList();

        dto.setOfertas(ofertasFiltradas);

        return dto;
    }





    // =========================
    // CREAR PRODUCTO COMPLETO DESDE DTO
    // =========================
    @Transactional
    public Productos crearProductoDesdeDTO(ProductoDTO dto) {
        Productos producto = new Productos();

        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());

        BigDecimal precioBase = dto.getPrecio() != null ? dto.getPrecio() : BigDecimal.ZERO;
        BigDecimal precioMayorista = dto.getPrecioMayorista() != null
                ? dto.getPrecioMayorista()
                : precioBase;

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

        producto = productoRepository.save(producto);

        if (dto.getFragancias() != null && !dto.getFragancias().isEmpty()) {
            List<Fragancia> fragancias = dto.getFragancias().stream()
                    .map(nombre -> fraganciaRepository.findByNombre(nombre)
                            .orElseGet(() -> fraganciaRepository.save(new Fragancia(nombre))))
                    .toList();
            producto.setFragancias(fragancias);
        }

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

        return productoRepository.save(producto);
    }

    // =========================
    // NUEVO MÉTODO: AGREGAR O SUMAR STOCK
    // =========================
    @Transactional
    public Productos agregarOActualizarProducto(ProductoDTO dto) {
        Optional<Productos> existenteOpt = productoRepository.findByNombre(dto.getNombre());

        if (existenteOpt.isPresent()) {
            Productos existente = existenteOpt.get();

            // Actualizar stock y total ingresado
            int stockNuevo = (existente.getStock() != null ? existente.getStock() : 0)
                    + (dto.getStock() != null ? dto.getStock() : 0);
            int totalNuevo = (existente.getTotalIngresado() != null ? existente.getTotalIngresado() : 0)
                    + (dto.getTotalIngresado() != null ? dto.getTotalIngresado() : 0);
            existente.setStock(stockNuevo);
            existente.setTotalIngresado(totalNuevo);

            // Actualizar datos básicos
            if (dto.getPrecio() != null) existente.setPrecio(dto.getPrecio());
            if (dto.getPrecioMayorista() != null) existente.setPrecioMayorista(dto.getPrecioMayorista());
            if (dto.getDescripcion() != null) existente.setDescripcion(dto.getDescripcion());
            if (dto.getImagenUrl() != null) existente.setImagenUrl(dto.getImagenUrl());

            // Categoría
            if (dto.getCategoriaNombre() != null && !dto.getCategoriaNombre().isBlank()) {
                Categoria cat = categoriaRepository.findByNombre(dto.getCategoriaNombre())
                        .orElseGet(() -> {
                            Categoria nueva = new Categoria();
                            nueva.setNombre(dto.getCategoriaNombre());
                            return categoriaRepository.save(nueva);
                        });
                existente.setCategoria(cat);
                existente.setIdCategoria(cat.getId());
            }

            // Fragancias: agregar nuevas sin eliminar existentes
            if (dto.getFragancias() != null && !dto.getFragancias().isEmpty()) {
                Set<String> nombresExistentes = existente.getFragancias().stream()
                        .map(Fragancia::getNombre)
                        .collect(Collectors.toSet());

                List<Fragancia> fraganciasActualizadas = dto.getFragancias().stream()
                        .filter(nombre -> !nombresExistentes.contains(nombre)) // solo nuevas
                        .map(nombre -> fraganciaRepository.findByNombre(nombre)
                                .orElseGet(() -> fraganciaRepository.save(new Fragancia(nombre))))
                        .toList();

                existente.getFragancias().addAll(fraganciasActualizadas);
            }

            // Atributos: actualizar individualmente
            if (dto.getAtributos() != null) {
                Map<String, ProductoAtributo> actuales = existente.getProductoAtributos().stream()
                        .collect(Collectors.toMap(pa -> pa.getAtributo().getNombre(), pa -> pa));

                Set<ProductoAtributo> nuevosAtributos = new HashSet<>();

                for (ProductoDTO.ProductoAtributoDTO aDto : dto.getAtributos()) {
                    Atributo atributo = atributoRepository.findByNombre(aDto.getNombre())
                            .orElseGet(() -> atributoRepository.save(new Atributo(aDto.getNombre())));

                    if (actuales.containsKey(aDto.getNombre())) {
                        // actualizar valor existente
                        actuales.get(aDto.getNombre()).setValor(aDto.getValor());
                        nuevosAtributos.add(actuales.get(aDto.getNombre()));
                    } else {
                        // agregar nuevo atributo
                        ProductoAtributo pa = new ProductoAtributo(existente, atributo, aDto.getValor());
                        nuevosAtributos.add(pa);
                    }
                }

                existente.setProductoAtributos(nuevosAtributos);
            }

            return productoRepository.save(existente);
        }

        return crearProductoDesdeDTO(dto);
    }


    //recibe el ID del producto y la cantidad vendida, y actualiza
    // solo el stock:
    @Transactional
    public Productos venderProducto(Long productoId, int cantidadVendida) {
        Productos producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (cantidadVendida <= 0) {
            throw new IllegalArgumentException("La cantidad vendida debe ser mayor a cero");
        }

        if (producto.getStock() < cantidadVendida) {
            throw new IllegalArgumentException("No hay suficiente stock para vender la cantidad solicitada");
        }

        producto.setStock(producto.getStock() - cantidadVendida);

        return productoRepository.save(producto);
    }

}
