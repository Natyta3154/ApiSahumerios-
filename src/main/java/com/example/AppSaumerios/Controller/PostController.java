package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.Service.PostService;
import com.example.AppSaumerios.dto.PostDTO;
import com.example.AppSaumerios.entity.Post;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "https://app-aroman.vercel.app")
public class PostController {

    private final PostService service;

    public PostController(PostService service) {
        this.service = service;
    }

    /**
     * 🔹 Listar todos los posts (público)
     */
    @GetMapping
    public List<PostDTO> getAllPosts() {
        return service.getAllPosts();
    }

    /**
     * 🔹 Obtener un post por ID (público)
     */
    @GetMapping("/{id}")
    public PostDTO getPost(@PathVariable Long id) {
        return service.getPostById(id);
    }

    /**
     * 🔹 Crear un nuevo post (solo ADMIN)
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/agregarPost")
    public PostDTO createPost(@RequestBody Post post) {
        return service.savePost(post);
    }

    /**
     * 🔹 Actualizar un post (solo ADMIN)
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/actualizarPost/{id}")
    public PostDTO updatePost(@PathVariable Long id, @RequestBody Post postActualizado) {
        return service.updatePost(id, postActualizado);
    }

    /**
     * 🔹 Eliminar un post (solo ADMIN)
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/eliminar/{id}")
    public void deletePost(@PathVariable Long id) {
        service.deletePost(id);
    }
}
