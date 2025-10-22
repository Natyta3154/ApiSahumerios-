package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.dto.CategoriaDTO;
import com.example.AppSaumerios.Service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(
        origins = {
                "http://localhost:9002",
                "https://front-sahumerios-2.vercel.app",
                "https://app-sahumerio3.vercel.app"
        },
        allowCredentials = "true"
)
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // Listar todas las categorías
    @GetMapping("/listado")
    public ResponseEntity<List<CategoriaDTO>> listarTodas() {
        List<CategoriaDTO> categorias = categoriaService.listarTodas();
        return ResponseEntity.ok().body(categorias);
    }


    // Agregar categoría
    @PostMapping("/agregar")
    public ResponseEntity<CategoriaDTO> agregar(@Valid @RequestBody CategoriaDTO dto) {
        return ResponseEntity.ok(categoriaService.guardar(dto));
    }

    // Editar categoría
    @PutMapping("/editar/{id}")
    public ResponseEntity<CategoriaDTO> editar(@PathVariable Long id,
                                               @Valid @RequestBody CategoriaDTO dto) {
        return ResponseEntity.ok(categoriaService.actualizar(id, dto));
    }

    // Eliminar categoría
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.ok().build();
    }
}

