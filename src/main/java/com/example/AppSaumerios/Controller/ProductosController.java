package com.example.AppSaumerios.Controller;


import com.example.AppSaumerios.Service.ProductosServices;
import com.example.AppSaumerios.dto.ProductoDTO;
import com.example.AppSaumerios.dto.ProductoUpdateDTO;
import com.example.AppSaumerios.entity.Atributo;
import com.example.AppSaumerios.entity.Categoria;
import com.example.AppSaumerios.entity.Fragancia;
import com.example.AppSaumerios.entity.Productos;
import com.example.AppSaumerios.repository.AtributoRepository;
import com.example.AppSaumerios.repository.CategoriaRepository;
import com.example.AppSaumerios.repository.FraganciaRepository;
import com.example.AppSaumerios.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
@CrossOrigin(origins = "http://localhost:9002")
@RestController
@RequestMapping("/productos")
public class ProductosController {

    @Autowired
    private ProductosServices productoservices;

    @Autowired
    FraganciaRepository fraganciaRepository;

    @Autowired
    CategoriaRepository categoriaRepository;

    @Autowired
    ProductoRepository productoRepository;
    @Autowired
    AtributoRepository atributoRepository;

    // -------------------
    // ENDPOINTS PÚBLICOS
    // -------------------
    @GetMapping("/listado")
    public List<ProductoDTO> listarTodos() {
        return productoservices.listarTodosDTO();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> buscarPorId(@PathVariable Long id) {
        return productoservices.buscarPorId(id)
                .map(producto -> ResponseEntity.ok(productoservices.mapToDTO(producto)))
                .orElse(ResponseEntity.notFound().build());
    }

    // -------------------
    // ENDPOINTS ADMIN (solo /admin/productos/**)
    // -------------------
    @PostMapping("/agregar")
    public ResponseEntity<ProductoDTO> crearProducto(@RequestBody ProductoDTO request) {


        // 1️⃣ Verificar si el producto ya existe por nombre
        List<Productos> productosExistentes = productoRepository.findAllByNombre(request.getNombre());

        Optional<Productos> productoExistente = productoRepository.findByNombre(request.getNombre());

        if (productoExistente.isPresent()) {
            ProductoDTO dto = productoservices.mapToDTO(productoExistente.get());
            dto.setMensaje("El producto ya está en la base de datos");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(dto);
        }


        // Crear producto normalmente
        Productos producto = new Productos();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setImagenurl(request.getImagenUrl());
        producto.setActivo(request.getActivo());

        // Buscar categoría por nombre; si no existe, crearla automáticamente
        Categoria categoria = categoriaRepository.findByNombre(request.getCategoriaNombre())
                .orElseGet(() -> {
                    Categoria nuevaCategoria = new Categoria();
                    nuevaCategoria.setNombre(request.getCategoriaNombre());
                    nuevaCategoria.setDescripcion("Creada automáticamente al agregar producto");
                    return categoriaRepository.save(nuevaCategoria);
                });

        producto.setCategoria(categoria);
        producto.setIdCategoria(categoria.getId());

        // Guardar producto para poder asignar relaciones
        Productos productoGuardado = productoRepository.save(producto);

        // Asignar fragancias
        List<Fragancia> fragancias = new ArrayList<>();
        for (String nombreFragancia : request.getFragancias()) {
            Fragancia fragancia = fraganciaRepository.findByNombre(nombreFragancia)
                    .orElseGet(() -> {
                        Fragancia nueva = new Fragancia();
                        nueva.setNombre(nombreFragancia);
                        return fraganciaRepository.save(nueva);
                    });
            fragancias.add(fragancia);
        }
        producto.setFragancias(fragancias);

// ========================
// Asignar atributos
// ========================
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

        ProductoDTO dto = productoservices.mapToDTO(producto);
        return ResponseEntity.ok(dto);
    }




    @PutMapping("/editar/{id}")
    public ResponseEntity<Productos> actualizarProducto(
            @PathVariable Long id,
            @RequestBody ProductoUpdateDTO dto) {

        Productos actualizado = productoservices.actualizarProductos(
                id,
                dto.toProductos(),
                dto.getPorcentajeDescuento(),
                dto.getFechaInicioDescuento(),
                dto.getFechaFinDescuento()
        );

        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id) {
        productoservices.eliminarProductos(id);
        return ResponseEntity.ok().build();
    }
}
