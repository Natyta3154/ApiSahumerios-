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
                "https://app-aroman.vercel.app"// tu dominio de producción
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

    @Autowired
    private ProductoMapper productoMapper;

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
        List<OfertaDTO> todasLasOfertas = ofertaService.obtenerTodasLasOfertasDTO();

        return productos.stream()
                .map(producto -> ProductoMapper.toDTO(producto, todasLasOfertas))
                .collect(Collectors.toList());
    }



    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> buscarPorId(@PathVariable Long id) {
        try {
            // 1. CORRECCIÓN: Llamar al servicio y esperar la entidad Productos directamente.
            Productos producto = productoservice.buscarPorId(id);

            List<OfertaDTO> todasLasOfertas = ofertaService.obtenerTodasLasOfertasDTO();

            // Mapeo final (Usando la entidad 'producto' directamente)
            ProductoDTO dto = ProductoMapper.toDTO(producto, todasLasOfertas);
            return ResponseEntity.ok(dto);

        } catch (NoSuchElementException ex) {
            // 2. Manejo de error: El servicio lanza esta excepción si no encuentra el producto (404 Not Found)
            return ResponseEntity.notFound().build();
        }
    }




    // -------------------
// ENDPOINTS DESTACADOS / TOP 4
// -------------------

    // Top 5 por categoría
    @GetMapping("/top5/{categoriaId}")
    public ResponseEntity<List<ProductoDestacadoDTO>> top5PorCategoria(@PathVariable Long categoriaId) {
        List<ProductoDestacadoDTO> productos = productoservice.obtenerTop5PorCategoria(categoriaId);
        if (productos.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(productos);
    }

    // Top 5 generales
    @GetMapping("/top5")
    public ResponseEntity<List<ProductoDestacadoDTO>> top5Generales(Long categoriaId) {
        List<ProductoDestacadoDTO> productos = productoservice.obtenerTop5Generales();
        if (productos.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(productos);
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
    public ResponseEntity<ProductoDTO> agregarProducto(@Valid @RequestBody ProductoDTO dto) {
        try {
            // 📢 DELEGACIÓN: El Service se encarga de toda la lógica de validación, creación y actualización.
            Productos productoCreadoOActualizado = productoservice.crearOActualizarProducto(dto);

            // 2. Mapear la entidad resultante para la respuesta (siempre debe ir en el Controller/Facade)
            List<OfertaDTO> todasLasOfertas = ofertaService.obtenerTodasLasOfertasDTO();
            ProductoDTO dtoResponse = productoMapper.toDTO(productoCreadoOActualizado, todasLasOfertas);

            // El Service puede devolver el mensaje, o lo definimos aquí si solo es éxito.
            dtoResponse.setMensaje("Producto procesado correctamente (creado o stock actualizado)");
            return ResponseEntity.ok(dtoResponse);

        } catch (IllegalArgumentException ex) {
            ProductoDTO errorDto = new ProductoDTO();
            errorDto.setMensaje(ex.getMessage());
            return ResponseEntity.badRequest().body(errorDto);
        }
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