package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.Service.OfertaService;
import com.example.AppSaumerios.dto.OfertaDTO;
import com.example.AppSaumerios.dto.ProductoOfertaDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/ofertas")
@CrossOrigin(origins = {
        "http://localhost:9002",
        "http://localhost:3000",
        "https://api-sahumerios.vercel.app",
        "https://hernan.alwaysdata.net"
})
public class OfertaController {

    private final OfertaService ofertaService;

    public OfertaController(OfertaService ofertaService) {
        this.ofertaService = ofertaService;
    }

    // =========================
    // Listar todas las ofertas (DTO) - Público
    // =========================
    @GetMapping("/listar")
    public ResponseEntity<List<OfertaDTO>> listarOfertas() {
        List<OfertaDTO> ofertas = ofertaService.listarOfertasDTO();
        return ResponseEntity.ok(ofertas);
    }

    // =========================
    // Listar productos con precio final aplicado - Público
    // =========================
    @GetMapping("/con-precio")
    public ResponseEntity<List<ProductoOfertaDTO>> listarOfertasConPrecioFinal() {
        List<ProductoOfertaDTO> productos = ofertaService.listarOfertasConPrecioFinal();
        return ResponseEntity.ok(productos);
    }

    // =========================
    // Buscar oferta por ID - Público
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
    // Crear nueva oferta - Solo Admin
    // =========================
    @PostMapping("/crearOferta")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<OfertaDTO> crearOferta(@RequestBody OfertaDTO dto) {
        return ResponseEntity.ok(ofertaService.crearOfertaDTO(dto));
    }

    // =========================
    // Actualizar oferta existente - Solo Admin
    // =========================
    @PutMapping("/editar/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<OfertaDTO> actualizar(@PathVariable Long id, @RequestBody OfertaDTO dto) {
        OfertaDTO ofertaExistente = ofertaService.buscarPorIdDTO(id);
        if (ofertaExistente == null) {
            return ResponseEntity.notFound().build();
        }

        // Mantener producto y ID de la oferta intactos
        dto.setProductoId(ofertaExistente.getProductoId());
        dto.setIdOferta(id);

        return ResponseEntity.ok(ofertaService.crearOfertaDTO(dto));
    }

    // =========================
    // Eliminar oferta - Solo Admin
    // =========================
    @DeleteMapping("/eliminar/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        OfertaDTO ofertaExistente = ofertaService.buscarPorIdDTO(id);
        if (ofertaExistente != null) {
            ofertaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}


