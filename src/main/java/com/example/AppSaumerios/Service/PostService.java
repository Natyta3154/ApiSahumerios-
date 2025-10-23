package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.entity.Post;
import com.example.AppSaumerios.entity.CategoriaBlog;
import com.example.AppSaumerios.repository.CategoriaBlogRepository;
import com.example.AppSaumerios.repository.PostRepository;
import com.example.AppSaumerios.dto.PostDTO;
import com.example.AppSaumerios.dto.CategoriaBlogDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository repository;
    private final CategoriaBlogRepository categoriaRepository;

    public PostService(PostRepository repository, CategoriaBlogRepository categoriaRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
    }

    /** 🔹 Obtener todos los posts como DTOs (público) */
    public List<PostDTO> getAllPosts() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /** 🔹 Obtener un post por ID como DTO (público) */
    public PostDTO getPostById(Long id) {
        Post post = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));
        return convertToDTO(post);
    }

    /** 🔹 Crear un post (ADMIN) */
    public PostDTO savePost(Post post) {
        Long catId = post.getCategoria() != null ? post.getCategoria().getId() : null;
        if (catId == null) {
            throw new RuntimeException("Debe asignarse una categoría al post");
        }

        CategoriaBlog categoria = categoriaRepository.findById(catId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + catId));
        post.setCategoria(categoria);
        post.setFechaActualizacion(LocalDateTime.now());

        Post saved = repository.save(post);
        return convertToDTO(saved);
    }

    public PostDTO updatePost(Long id, Post postActualizado) {
        Post existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post no encontrado con ID: " + id));

        existente.setTitulo(postActualizado.getTitulo());
        existente.setDescripcion(postActualizado.getDescripcion());
        existente.setContenido(postActualizado.getContenido());
        existente.setImagenUrl(postActualizado.getImagenUrl());

        Long catId = postActualizado.getCategoria() != null ? postActualizado.getCategoria().getId() : null;
        if (catId != null) {
            CategoriaBlog categoria = categoriaRepository.findById(catId)
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + catId));
            existente.setCategoria(categoria);
        }

        existente.setFechaActualizacion(LocalDateTime.now());
        Post updated = repository.save(existente);

        return convertToDTO(updated);
    }
    /** 🔹 Eliminar un post (ADMIN) */
    public void deletePost(Long id) {
        repository.deleteById(id);
    }


    private PostDTO convertToDTO(Post post) {
        CategoriaBlogDTO catDTO = new CategoriaBlogDTO();
        catDTO.setId(post.getCategoria().getId());
        catDTO.setNombre(post.getCategoria().getNombre());
        catDTO.setDescripcion(post.getCategoria().getDescripcion());

        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitulo(post.getTitulo());
        dto.setDescripcion(post.getDescripcion());
        dto.setContenido(post.getContenido());
        dto.setImagenUrl(post.getImagenUrl());
        dto.setCategoria(catDTO);
        dto.setFechaCreacion(post.getFechaCreacion());
        dto.setFechaActualizacion(post.getFechaActualizacion());
        return dto;
    }

}
