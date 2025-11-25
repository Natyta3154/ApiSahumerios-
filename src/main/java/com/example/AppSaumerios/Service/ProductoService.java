package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.dto.*;
import com.example.AppSaumerios.entity.*;
import com.example.AppSaumerios.repository.*;
import com.example.AppSaumerios.util.ProductoMapper;
import org.springframework.cache.annotation.Cacheable; // <-- Importación para lectura
import org.springframework.cache.annotation.CacheEvict;  // <-- Importación para limpieza
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ProductoService {

    // Se asume que las cachés a limpiar son: "productosTop" y "productosDestacados"
   // private static final String[] CACHE_NOMBRES = {"productosTop", "productosDestacados"};

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

    @Autowired
    private ProductoMapper productoMapper;

    // =========================
    // CRUD Básico
    // =========================
    public List<Productos> listarTodos() {
        return productoRepository.findAll();
    }

    public Productos buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado con ID: " + id));
    }

    @CacheEvict(value = {"productosTop", "productosDestacados"}, allEntries = true) // Limpia caché al guardar/actualizar
    public Productos guardarProductos(Productos productos) {
        validarStockYPrecio(productos);
        return productoRepository.save(productos);
    }

    // =========================
    // LÓGICA DE NEGOCIO CRÍTICA: Crear o Actualizar Stock (Migrado del Controller)
    // =========================
    @CacheEvict(value = {"productosTop", "productosDestacados"}, allEntries = true) // Limpia caché al crear/actualizar stock
    @Transactional
    public Productos crearOActualizarProducto(ProductoDTO request) {

        // 1. Lógica de inventario: Buscar por nombre
        Optional<Productos> productoExistente = productoRepository.findByNombre(request.getNombre());

        if (productoExistente.isPresent()) {
            Productos producto = productoExistente.get();

            // 1.1. Sumar stock y total ingresado
            int stockNuevo = request.getStock() != null ? request.getStock() : 0;
            int totalNuevo = request.getTotalIngresado() != null ? request.getTotalIngresado() : stockNuevo;

            producto.setStock(producto.getStock() + stockNuevo);
            producto.setTotalIngresado(producto.getTotalIngresado() + totalNuevo);

            // 1.2. Validar y Guardar (Actualización simple)
            validarStockYPrecio(producto);
            return productoRepository.save(producto);
        }

        // 2. Lógica de Creación (si no existe)
        Productos producto = new Productos();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setPrecioMayorista(request.getPrecioMayorista() != null ? request.getPrecioMayorista() : request.getPrecio());
        producto.setStock(request.getStock() != null ? request.getStock() : 0);
        producto.setTotalIngresado(request.getTotalIngresado() != null ? request.getTotalIngresado() : producto.getStock());
        producto.setImagenUrl(request.getImagenUrl());
        producto.setActivo(request.getActivo() != null ? request.getActivo() : true);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setDestacado(request.isDestacado());

        // 3. Asignar Categoría (Buscar o crear)
        Categoria categoria = categoriaRepository.findByNombre(request.getCategoriaNombre())
                .orElseGet(() -> {
                    Categoria nuevaCategoria = new Categoria();
                    nuevaCategoria.setNombre(request.getCategoriaNombre());
                    return categoriaRepository.save(nuevaCategoria);
                });
        producto.setCategoria(categoria);
        // Si la columna idCategoria sigue siendo usada, descomentar:
        // producto.setIdCategoria(categoria.getId());

        // 4. Asignar Fragancias (Buscar o crear)
        if (request.getFragancias() != null) {
            List<Fragancia> fragancias = request.getFragancias().stream()
                    .map(nombreFragancia -> fraganciaRepository.findByNombre(nombreFragancia)
                            .orElseGet(() -> {
                                Fragancia nueva = new Fragancia();
                                nueva.setNombre(nombreFragancia);
                                return fraganciaRepository.save(nueva);
                            }))
                    .collect(Collectors.toList());
            producto.setFragancias(fragancias);
        }

        // 5. Asignar Atributos (Buscar o crear)
        if (request.getAtributos() != null) {
            if (producto.getProductoAtributos() == null) {
                producto.setProductoAtributos(new ArrayList<>());
            }
            producto.getProductoAtributos().clear(); // Limpiar si se llamó dos veces

            for (ProductoAtributoDTO attrDTO : request.getAtributos()) {
                String nombreAttr = attrDTO.getNombre();
                String valorAttr = attrDTO.getValor();

                if (nombreAttr != null && valorAttr != null) {
                    Atributo atributo = atributoRepository.findByNombre(nombreAttr)
                            .orElseGet(() -> {
                                Atributo nuevo = new Atributo();
                                nuevo.setNombre(nombreAttr);
                                return atributoRepository.save(nuevo);
                            });

                    ProductoAtributo pa = new ProductoAtributo(producto, atributo, valorAttr);
                    producto.getProductoAtributos().add(pa);
                }
            }
        }

        // 6. Guardar el producto final
        validarStockYPrecio(producto);
        return productoRepository.save(producto);
    }


    // =========================
    // Productos destacados y relacionados
    // =========================
    // No cacheable: listar relacionados suele ser muy específico por producto y no se repite tanto
    public List<ProductoResumenDTO> listarRelacionados(Long categoriaId, Long excludeId) {
        List<Productos> productos = productoRepository
                .findTop4ByActivoTrueAndCategoria_IdOrderByPrecioDesc(categoriaId);
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

    // Cachable: cachea el resultado dependiendo del ID de la categoría (o 'general')
    @Cacheable(value = "productosDestacados", key = "#categoriaId != null ? #categoriaId : 'general'")
    @Transactional(readOnly = true)
    public List<Productos> obtenerProductosDestacados(Long categoriaId) {
        if (categoriaId != null) {
            return productoRepository.findTop4ByActivoTrueAndCategoria_IdOrderByPrecioDesc(categoriaId);
        } else {
            return productoRepository.findTop4ByActivoTrueOrderByPrecioDesc();
        }
    }

    // Cachable: cachea la lista general de DTOs destacados
    @Cacheable("productosTop")
    @Transactional(readOnly = true)
    public List<ProductoDTO> obtenerProductosDestacadosDTO() {
        List<Productos> productos = productoRepository.findDestacadosConRelaciones();
        return productos.stream()
                .limit(4)
                .map(p -> productoMapper.toDTO(p, null)) // Usando el mapper inyectado
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
    @CacheEvict(value = {"productosTop", "productosDestacados"}, allEntries = true) // Limpia caché al actualizar
    @Transactional
    public Productos actualizarCamposBasicos(Long id, ProductoUpdateDTO dto) {
        Productos producto = buscarPorId(id); // Uso del método corregido

        if (dto.getNombre() != null) producto.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) producto.setDescripcion(dto.getDescripcion());
        if (dto.getPrecio() != null) producto.setPrecio(dto.getPrecio());
        if (dto.getPrecioMayorista() != null) producto.setPrecioMayorista(dto.getPrecioMayorista());
        if (dto.getStock() != null) producto.setStock(dto.getStock());
        if (dto.getTotalIngresado() != null) producto.setTotalIngresado(dto.getTotalIngresado());
        if (dto.getActivo() != null) producto.setActivo(dto.getActivo());
        if (dto.getImagenUrl() != null) producto.setImagenUrl(dto.getImagenUrl());
        if (dto.getDestacado() != null) producto.setDestacado(dto.getDestacado());

        validarStockYPrecio(producto);
        return productoRepository.save(producto);
    }

    // Método pendiente de la corrección anterior
    @CacheEvict(value = {"productosTop", "productosDestacados"}, allEntries = true)// Limpia caché al actualizar masivamente
    @Transactional
    public void actualizarPreciosMasivos(Long categoriaId, BigDecimal precio, BigDecimal precioMayorista) {
        if (precio == null || precioMayorista == null || categoriaId == null) {
            throw new IllegalArgumentException("La categoría, el precio unitario y el precio mayorista no pueden ser nulos.");
        }
        productoRepository.actualizarPreciosPorCategoria(categoriaId, precio, precioMayorista);
    }


    // =========================
    // Actualizar categoría
    // =========================
    @CacheEvict(value = {"productosTop", "productosDestacados"}, allEntries = true) // Limpia caché
    @Transactional
    public Productos actualizarCategoria(Long id, String nombreCategoria) {
        Productos producto = buscarPorId(id); // Uso del método corregido

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
    @CacheEvict(value = {"productosTop", "productosDestacados"}, allEntries = true) // Limpia caché
    @Transactional
    public Productos actualizarFragancias(Long id, List<String> fraganciasNombres) {
        Productos producto = buscarPorId(id); // Uso del método corregido

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
    @CacheEvict(value = {"productosTop", "productosDestacados"}, allEntries = true) // Limpia caché
    @Transactional
    public Productos actualizarAtributos(Long id, List<ProductoAtributoDTO> atributosDTO) {
        // 📢 CORRECCIÓN: La firma ya no usa ProductoDTO.ProductoAtributoDTO

        Productos producto = buscarPorId(id);

        if (atributosDTO != null && !atributosDTO.isEmpty()) {
            // Limpiamos la lista existente antes de añadir los nuevos
            if (producto.getProductoAtributos() == null) {
                producto.setProductoAtributos(new ArrayList<>());
            }
            producto.getProductoAtributos().clear();

            for (ProductoAtributoDTO aDto : atributosDTO) {
                Atributo atributo = atributoRepository.findByNombre(aDto.getNombre())
                        .orElseGet(() -> {
                            Atributo a = new Atributo();
                            a.setNombre(aDto.getNombre());
                            return atributoRepository.save(a);
                        });

                // Creamos la nueva relación
                ProductoAtributo pa = new ProductoAtributo(producto, atributo, aDto.getValor());
                producto.getProductoAtributos().add(pa);
            }
        }

        return productoRepository.save(producto);
    }

    // =========================
    // Eliminar producto
    // =========================
    @CacheEvict(value = {"productosTop", "productosDestacados"}, allEntries = true) // Limpia caché al eliminar
    @Transactional
    public String eliminarProductos(Long id) {
        Productos producto = buscarPorId(id); // Uso del método corregido

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
    @CacheEvict(value = {"productosTop", "productosDestacados"}, allEntries = true) // Limpia caché al vender (cambia stock)
    @Transactional
    public Productos venderProducto(Long productoId, int cantidadVendida) {
        Productos producto = buscarPorId(productoId); // Uso del método corregido

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

    // Cachable: cachea la lista general de DTOs destacados
    @Cacheable("productosTop")
    public List<ProductoDestacadoDTO> obtenerTop5Generales() {
        return productoRepository.findTop4ByActivoTrueOrderByPrecioDesc()
                .stream()
                .map(productoMapper::toDestacadoDTO) // Usando el mapper inyectado
                .collect(Collectors.toList());
    }

    // Cachable: cachea la lista de DTOs destacados por categoría
    @Cacheable(value = "productosTop", key = "#categoriaId")
    public List<ProductoDestacadoDTO> obtenerTop5PorCategoria(Long categoriaId) {
        return productoRepository.findTop4ByActivoTrueAndCategoria_IdOrderByPrecioDesc(categoriaId)
                .stream()
                .map(productoMapper::toDestacadoDTO) // Usando el mapper inyectado
                .collect(Collectors.toList());
    }
}