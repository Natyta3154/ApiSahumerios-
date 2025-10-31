package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.Service.OfertaService;
import com.example.AppSaumerios.dto.OfertaDTO;
import com.example.AppSaumerios.dto.ProductoOfertaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(
        origins = {
                "http://localhost:9002",
                "https://app-aroman.vercel.app" // tu dominio de producción
        },
        allowCredentials = "true"
)
@RestController
@RequestMapping("/api/ofertas")

public class OfertaController {

    @Value("${frontend.url.${spring.profiles.active}}")
    private String frontendUrl;


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
// Obtener productos para el carrusel
// =========================
    @GetMapping("/carrusel")
    public ResponseEntity<List<ProductoOfertaDTO>> obtenerCarrusel(
            @RequestParam(name = "limite", defaultValue = "5") int limite) {

        List<ProductoOfertaDTO> carrusel = ofertaService.obtenerProductosCarrusel(limite);
        return ResponseEntity.ok(carrusel);
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
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/crearOferta")
    public ResponseEntity<OfertaDTO> crearOferta(@RequestBody OfertaDTO dto) {
        OfertaDTO creado = ofertaService.crearOfertaDTO(dto);
        return ResponseEntity.ok(creado);
    }

    // =========================
    // Actualizar oferta existente - Solo Admin
    // =========================
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/editar/{id}")
    public ResponseEntity<OfertaDTO> actualizar(@PathVariable Long id, @RequestBody OfertaDTO dto) {
        OfertaDTO ofertaExistente = ofertaService.buscarPorIdDTO(id);
        if (ofertaExistente == null) {
            return ResponseEntity.notFound().build();
        }

        // Mantener producto y ID de la oferta intactos
        dto.setProductoId(ofertaExistente.getProductoId());
        dto.setIdOferta(id);

        OfertaDTO actualizado = ofertaService.crearOfertaDTO(dto);
        return ResponseEntity.ok(actualizado);
    }

    // =========================
    // Eliminar oferta - Solo Admin
    // =========================
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        OfertaDTO ofertaExistente = ofertaService.buscarPorIdDTO(id);
        if (ofertaExistente != null) {
            ofertaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}


