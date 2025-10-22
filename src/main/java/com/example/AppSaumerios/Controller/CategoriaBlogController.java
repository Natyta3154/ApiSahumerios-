package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.Service.CategoriaBlogService;
import com.example.AppSaumerios.dto.CategoriaBlogDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias-blog")
@CrossOrigin(origins = "*")
public class CategoriaBlogController {

    private final CategoriaBlogService service;

    public CategoriaBlogController(CategoriaBlogService service) {
        this.service = service;
    }

    /**
     * 🔹 Listar todas las categorías (público)
     */
    @GetMapping("listarCategoriaBlog")
    public List<CategoriaBlogDTO> getAllCategorias() {
        return service.getAllCategorias();
    }

    /**
     * 🔹 Obtener categoría por ID (público)
     */
    @GetMapping("/{id}")
    public CategoriaBlogDTO getCategoria(@PathVariable Long id) {
        return service.getCategoriaById(id);
    }

    /**
     * 🔹 Crear categoría (solo ADMIN)
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("agregarCategoriaBlog")
    public CategoriaBlogDTO createCategoria(@RequestBody CategoriaBlogDTO categoriaDTO) {
        return service.saveCategoria(categoriaDTO);
    }

    /**
     * 🔹 Actualizar categoría (solo ADMIN)
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/Actualizar/{id}")
    public CategoriaBlogDTO updateCategoria(@PathVariable Long id, @RequestBody CategoriaBlogDTO categoriaDTO) {
        return service.updateCategoria(id, categoriaDTO);
    }

    /**
     * 🔹 Eliminar categoría (solo ADMIN)
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteCategoria(@PathVariable Long id) {
        service.deleteCategoria(id);
    }
}
