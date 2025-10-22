package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.dto.CategoriaBlogDTO;
import com.example.AppSaumerios.entity.CategoriaBlog;
import com.example.AppSaumerios.repository.CategoriaBlogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaBlogService {

    private final CategoriaBlogRepository repository;

    public CategoriaBlogService(CategoriaBlogRepository repository) {
        this.repository = repository;
    }

    /**
     * Listar todas las categorías (DTO)
     */
    public List<CategoriaBlogDTO> getAllCategorias() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener categoría por ID (DTO)
     */
    public CategoriaBlogDTO getCategoriaById(Long id) {
        CategoriaBlog cat = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));
        return toDTO(cat);
    }

    /**
     * Crear categoría (DTO)
     */
    public CategoriaBlogDTO saveCategoria(CategoriaBlogDTO dto) {
        CategoriaBlog cat = new CategoriaBlog();
        cat.setNombre(dto.getNombre());
        cat.setDescripcion(dto.getDescripcion());
        CategoriaBlog guardada = repository.save(cat);
        return toDTO(guardada);
    }

    /**
     * Actualizar categoría (DTO)
     */
    public CategoriaBlogDTO updateCategoria(Long id, CategoriaBlogDTO dto) {
        CategoriaBlog existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));

        existente.setNombre(dto.getNombre());
        existente.setDescripcion(dto.getDescripcion());

        CategoriaBlog actualizada = repository.save(existente);
        return toDTO(actualizada);
    }

    /**
     * Eliminar categoría
     */
    public void deleteCategoria(Long id) {
        repository.deleteById(id);
    }

    /**
     * Conversión de entidad a DTO
     */
    private CategoriaBlogDTO toDTO(CategoriaBlog cat) {
        CategoriaBlogDTO dto = new CategoriaBlogDTO();
        dto.setId(cat.getId());
        dto.setNombre(cat.getNombre());
        dto.setDescripcion(cat.getDescripcion());
        return dto;
    }
}

