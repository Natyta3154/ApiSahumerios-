package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.Service.OfertaService;
import com.example.AppSaumerios.dto.OfertaDTO;
import com.example.AppSaumerios.dto.ProductoOfertaDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ofertas")
@CrossOrigin(origins = "http://localhost:9002")
public class OfertaController {

    private final OfertaService ofertaService;

    public OfertaController(OfertaService ofertaService) {
        this.ofertaService = ofertaService;
    }

    // =========================
    // Listar todas las ofertas (DTO)
    // =========================
    @GetMapping("/listar")
    public ResponseEntity<List<OfertaDTO>> listarOfertas() {
        List<OfertaDTO> ofertas = ofertaService.listarOfertasDTO();
        return ResponseEntity.ok(ofertas);
    }

    // =========================
    // Listar productos con precio final aplicado
    // =========================
    @GetMapping("/con-precio")
    public ResponseEntity<List<ProductoOfertaDTO>> listarOfertasConPrecioFinal() {
        List<ProductoOfertaDTO> productos = ofertaService.listarOfertasConPrecioFinal();
        return ResponseEntity.ok(productos);
    }

    // =========================
    // Buscar oferta por ID
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<OfertaDTO> buscarPorId(@PathVariable Long id) {
        OfertaDTO oferta = ofertaService.buscarPorIdDTO(id);
        if (oferta != null) {
            return ResponseEntity.ok(oferta);
        }
        return ResponseEntity.notFound().build();
    }

    // =========================
    // Crear nueva oferta
    // =========================
    @PostMapping("/crearOferta")
    public ResponseEntity<OfertaDTO> crearOferta(@RequestBody OfertaDTO dto) {
        return ResponseEntity.ok(ofertaService.crearOfertaDTO(dto));
    }

    // =========================
    // Actualizar oferta existente
    // =========================
    @PutMapping("/editar/{id}")
    public ResponseEntity<OfertaDTO> actualizar(@PathVariable Long id, @RequestBody OfertaDTO dto) {
        OfertaDTO ofertaExistente = ofertaService.buscarPorIdDTO(id);
        if (ofertaExistente == null) {
            return ResponseEntity.notFound().build();
        }

        // Mantenemos el producto y el ID de la oferta
        dto.setProductoId(ofertaExistente.getProductoId());
        dto.setIdOferta(id);

        return ResponseEntity.ok(ofertaService.crearOfertaDTO(dto));
    }

    // =========================
    // Eliminar oferta
    // =========================
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (ofertaService.buscarPorIdDTO(id) != null) {
            ofertaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

