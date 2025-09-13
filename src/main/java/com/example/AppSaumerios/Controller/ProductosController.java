package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.Service.ProductoService;
import com.example.AppSaumerios.Service.OfertaService;
import com.example.AppSaumerios.dto.ProductoDTO;
import com.example.AppSaumerios.dto.ProductoOfertaDTO;
import com.example.AppSaumerios.dto.ProductoUpdateDTO;
import com.example.AppSaumerios.entity.Atributo;
import com.example.AppSaumerios.entity.Categoria;
import com.example.AppSaumerios.entity.Fragancia;
import com.example.AppSaumerios.entity.Productos;
import com.example.AppSaumerios.repository.AtributoRepository;
import com.example.AppSaumerios.repository.CategoriaRepository;
import com.example.AppSaumerios.repository.FraganciaRepository;
import com.example.AppSaumerios.repository.ProductoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:9002")
@RestController
@RequestMapping("/productos")
public class ProductosController {

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

    @GetMapping("/listado")
    public List<ProductoDTO> listarTodos() {
        List<Productos> productos = productoservice.listarTodos();
        List<ProductoOfertaDTO> todasLasOfertas = ofertaService.listarOfertasConPrecioFinal();

        return productos.stream()
                .map(producto -> productoservice.mapToDTO(producto, todasLasOfertas))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> buscarPorId(@PathVariable Long id) {
        Optional<Productos> productoOpt = productoservice.buscarPorId(id);
        if (productoOpt.isEmpty()) return ResponseEntity.notFound().build();

        List<ProductoOfertaDTO> todasLasOfertas = ofertaService.listarOfertasConPrecioFinal();
        ProductoDTO dto = productoservice.mapToDTO(productoOpt.get(), todasLasOfertas);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/vender/{id}")
    public ResponseEntity<ProductoDTO> venderProducto(
            @PathVariable Long id,
            @RequestParam int cantidad) {

        Productos productoActualizado = productoservice.venderProducto(id, cantidad);
        List<ProductoOfertaDTO> todasLasOfertas = ofertaService.listarOfertasConPrecioFinal();
        ProductoDTO dto = productoservice.mapToDTO(productoActualizado, todasLasOfertas);

        dto.setMensaje("Venta realizada, stock actualizado");
        return ResponseEntity.ok(dto);
    }

    // -------------------
    // ENDPOINTS ADMIN
    // -------------------

    @PostMapping("/agregar")
    public ResponseEntity<ProductoDTO> agregarProducto(@RequestBody ProductoDTO request) {
        List<ProductoOfertaDTO> todasLasOfertas = ofertaService.listarOfertasConPrecioFinal();

        Optional<Productos> productoExistente = productoRepository.findByNombre(request.getNombre());

        if (productoExistente.isPresent()) {
            Productos producto = productoExistente.get();
            int stockNuevo = request.getStock() != null ? request.getStock() : 0;
            int totalNuevo = request.getTotalIngresado() != null ? request.getTotalIngresado() : stockNuevo;

            producto.setStock(producto.getStock() + stockNuevo);
            producto.setTotalIngresado(producto.getTotalIngresado() + totalNuevo);

            productoRepository.save(producto);

            ProductoDTO dto = productoservice.mapToDTO(producto, todasLasOfertas);
            dto.setMensaje("Producto existente actualizado: stock y total ingresado sumados");
            return ResponseEntity.ok(dto);
        }

        Productos producto = new Productos();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setPrecioMayorista(request.getPrecioMayorista() != null ? request.getPrecioMayorista() : request.getPrecio());

        int stock = request.getStock() != null ? request.getStock() : 0;
        int totalIngresado = request.getTotalIngresado() != null ? request.getTotalIngresado() : stock;
        producto.setStock(stock);
        producto.setTotalIngresado(totalIngresado);

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

        ProductoDTO dto = productoservice.mapToDTO(producto, todasLasOfertas);
        dto.setMensaje("Producto agregado correctamente");
        return ResponseEntity.ok(dto);
    }
    @PutMapping("/editar/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(
            @PathVariable Long id,
            @RequestBody ProductoUpdateDTO dto) {
        // LOG PARA DEBUG
        System.out.println("DTO que se va a devolver al frontend:");
        ProductoDTO dtoResponse = null;
        System.out.println(dtoResponse);


        // 1. Actualiza el producto usando tu service
        Productos actualizado = productoservice.actualizarProductos(id, dto);

        // 2. Obtiene todas las ofertas activas para mapear correctamente
        List<ProductoOfertaDTO> todasLasOfertas = ofertaService.listarOfertasConPrecioFinal();

        // 3. Mapea la entidad a DTO para retornar toda la info actualizada
        dtoResponse = productoservice.mapToDTO(actualizado, todasLasOfertas);

        // 4. Devuelve DTO completo
        return ResponseEntity.ok(dtoResponse);
    }





    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id) {
        productoservice.eliminarProductos(id);
        return ResponseEntity.ok().build();
    }
}
