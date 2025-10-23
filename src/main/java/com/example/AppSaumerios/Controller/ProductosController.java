package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.Service.ProductoService;
import com.example.AppSaumerios.Service.OfertaService;
import com.example.AppSaumerios.dto.*;
import com.example.AppSaumerios.entity.Atributo;
import com.example.AppSaumerios.entity.Categoria;
import com.example.AppSaumerios.entity.Fragancia;
import com.example.AppSaumerios.entity.Productos;
import com.example.AppSaumerios.repository.AtributoRepository;
import com.example.AppSaumerios.repository.CategoriaRepository;
import com.example.AppSaumerios.repository.FraganciaRepository;
import com.example.AppSaumerios.repository.ProductoRepository;
import com.example.AppSaumerios.util.ProductoMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
//import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/productos")
@CrossOrigin(
        origins = {
                "http://localhost:9002",
                "https://front-sahumerios-2.vercel.app",
                "https://app-sahumerio3.vercel.app" // tu dominio de producción
        },
        allowCredentials = "true"
)
public class ProductosController {

    @Value("${frontend.url.${spring.profiles.active}}")
    private String frontendUrl;


    @Autowired
    private ProductoService productoservice;




    @Autowired
    private OfertaService ofertaService;

    @Autowired
    private FraganciaRepository fraganciaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private AtributoRepository atributoRepository;

    // -------------------
    // ENDPOINTS PÚBLICOS
    // -------------------


    //Endpoint para el resumen de los productos
    @GetMapping("/resumen")
    public Page<ProductoResumenDTO> listarProductosResumen(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return productoservice.listarResumen(pageable); // ✅ ahora sí
    }

    @GetMapping("/relacionados")
    public List<ProductoResumenDTO> listarRelacionados(
            @RequestParam Long categoriaId,
            @RequestParam(required = false) Long excludeId) {
        return productoservice.listarRelacionados(categoriaId, excludeId);
    }



    @GetMapping("/listado")
    public List<ProductoDTO> listarTodos() {
        List<Productos> productos = productoservice.listarTodos();

        // Convertir ProductoOfertaDTO a OfertaDTO
        List<OfertaDTO> todasLasOfertas = ofertaService.listarOfertasConPrecioFinal().stream()
                .map(of -> {
                    OfertaDTO dto = new OfertaDTO();
                    dto.setIdOferta(of.getId());
                    dto.setProductoId(of.getId()); // <-- acá necesitás un método que devuelva el productoId
                    dto.setValorDescuento(of.getPrecioOriginal().subtract(of.getPrecioConDescuento()));
                    dto.setTipoDescuento("MONTO");
                    dto.setFechaInicio(of.getFechaInicio());
                    dto.setFechaFin(of.getFechaFin());
                    dto.setEstado(true);
                    dto.setNombreProducto(of.getNombre());
                    dto.setPrecio(of.getPrecioOriginal());
                    return dto;
                })
                .collect(Collectors.toList());


        return productos.stream()
                .map(producto -> ProductoMapper.toDTO(producto, todasLasOfertas))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> buscarPorId(@PathVariable Long id) {
        Optional<Productos> productoOpt = productoservice.buscarPorId(id);
        if (productoOpt.isEmpty()) return ResponseEntity.notFound().build();

        // Convertir ProductoOfertaDTO a OfertaDTO
        List<OfertaDTO> todasLasOfertas = ofertaService.listarOfertasConPrecioFinal().stream()
                .map(of -> {
                    OfertaDTO dto = new OfertaDTO();
                    dto.setIdOferta(of.getId());
                    dto.setProductoId(of.getId());
                    dto.setValorDescuento(of.getPrecioOriginal().subtract(of.getPrecioConDescuento()));
                    dto.setTipoDescuento("MONTO");
                    dto.setFechaInicio(of.getFechaInicio());
                    dto.setFechaFin(of.getFechaFin());
                    dto.setEstado(true);
                    dto.setNombreProducto(of.getNombre());
                    dto.setPrecio(of.getPrecioOriginal());
                    return dto;
                })
                .collect(Collectors.toList());

        ProductoDTO dto = ProductoMapper.toDTO(productoOpt.get(), todasLasOfertas);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/destacados")
    public ResponseEntity<?> obtenerProductosDestacados() {
        try {
            List<ProductoDTO> destacados = productoservice.obtenerProductosDestacadosDTO();
            return ResponseEntity.ok(destacados);
        } catch (Exception e) {
            e.printStackTrace(); // log completo del error
            return ResponseEntity.status(500)
                    .body(Map.of(
                            "status", 500,
                            "error", "Internal Server Error",
                            "mensaje", e.getMessage(),
                            "timestamp", LocalDateTime.now()
                    ));
        }
    }


    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/vender/{id}")
    public ResponseEntity<ProductoDTO> venderProducto(
            @PathVariable Long id,
            @RequestParam int cantidad) {

        Productos productoActualizado = productoservice.venderProducto(id, cantidad);
        List<OfertaDTO> todasLasOfertas = ofertaService.obtenerTodasLasOfertasDTO();
        ProductoDTO dto = ProductoMapper.toDTO(productoActualizado, todasLasOfertas);

        dto.setMensaje("Venta realizada, stock actualizado");
        return ResponseEntity.ok(dto);
    }

    // -------------------
    // ENDPOINTS ADMIN
    // -------------------

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/agregar")
    public ResponseEntity<ProductoDTO> agregarProducto(@RequestBody ProductoDTO request) {
        List<OfertaDTO> todasLasOfertas = ofertaService.obtenerTodasLasOfertasDTO();

        Optional<Productos> productoExistente = productoRepository.findByNombre(request.getNombre());

        if (productoExistente.isPresent()) {
            Productos producto = productoExistente.get();
            int stockNuevo = request.getStock() != null ? request.getStock() : 0;
            int totalNuevo = request.getTotalIngresado() != null ? request.getTotalIngresado() : stockNuevo;

            producto.setStock(producto.getStock() + stockNuevo);
            producto.setTotalIngresado(producto.getTotalIngresado() + totalNuevo);

            productoRepository.save(producto);

            ProductoDTO dto = ProductoMapper.toDTO(producto, todasLasOfertas);
            dto.setMensaje("Producto existente actualizado: stock y total ingresado sumados");
            return ResponseEntity.ok(dto);
        }

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

        // Categoría
        Categoria categoria = categoriaRepository.findByNombre(request.getCategoriaNombre())
                .orElseGet(() -> {
                    Categoria nuevaCategoria = new Categoria();
                    nuevaCategoria.setNombre(request.getCategoriaNombre());
                    nuevaCategoria.setDescripcion("Creada automáticamente al agregar producto");
                    return categoriaRepository.save(nuevaCategoria);
                });
        producto.setCategoria(categoria);
        producto.setIdCategoria(categoria.getId());

        // Guardar producto antes de relaciones
        producto = productoRepository.save(producto);

        // Fragancias
        List<Fragancia> fragancias = new ArrayList<>();
        if (request.getFragancias() != null) {
            for (String nombreFragancia : request.getFragancias()) {
                Fragancia fragancia = fraganciaRepository.findByNombre(nombreFragancia)
                        .orElseGet(() -> {
                            Fragancia nueva = new Fragancia();
                            nueva.setNombre(nombreFragancia);
                            return fraganciaRepository.save(nueva);
                        });
                fragancias.add(fragancia);
            }
        }
        producto.setFragancias(fragancias);

        // Atributos
        if (request.getAtributos() != null) {
            for (ProductoDTO.ProductoAtributoDTO attrDTO : request.getAtributos()) {
                String nombreAttr = attrDTO.getNombre();
                String valorAttr = attrDTO.getValor();
                if (nombreAttr != null && valorAttr != null) {
                    Atributo atributo = atributoRepository.findByNombre(nombreAttr)
                            .orElseGet(() -> {
                                Atributo nuevo = new Atributo();
                                nuevo.setNombre(nombreAttr);
                                return atributoRepository.save(nuevo);
                            });
                    producto.addAtributo(atributo, valorAttr);
                }
            }
        }

        productoRepository.save(producto);

        ProductoDTO dto = ProductoMapper.toDTO(producto, todasLasOfertas);
        dto.setMensaje("Producto agregado correctamente");
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/editar/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(
            @PathVariable Long id,
            @RequestBody ProductoUpdateDTO dto) {

        Productos actualizado = productoservice.actualizarCamposBasicos(id, dto);
        List<OfertaDTO> todasLasOfertas = ofertaService.obtenerTodasLasOfertasDTO();
        ProductoDTO dtoResponse = ProductoMapper.toDTO(actualizado, todasLasOfertas);

        return ResponseEntity.ok(dtoResponse);
    }


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Map<String, String>> eliminarProducto(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        try {
            String mensaje = productoservice.eliminarProductos(id);
            response.put("mensaje", mensaje);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            response.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }



}