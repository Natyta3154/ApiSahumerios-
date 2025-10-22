package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.entity.Fragancia;
import com.example.AppSaumerios.repository.FraganciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fragancias")
@CrossOrigin(origins = {"http://localhost:9002", "https://front-sahumerios-2.vercel.app"}, allowCredentials = "true")
public class FraganciaController {

    @Autowired
    private FraganciaRepository fraganciaRepository;

    // 📌 Listado de todas las fragancias
    @GetMapping("/listadoFragancias")
    public List<Fragancia> listarTodas() {
        return fraganciaRepository.findAll();
    }

    // 📌 Agregar nueva fragancia
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/agregarFragancia")
    public ResponseEntity<Fragancia> agregar(@RequestBody Fragancia fragancia) {
        if (fraganciaRepository.existsByNombre(fragancia.getNombre())) {
            return ResponseEntity.badRequest().build();
        }
        Fragancia guardada = fraganciaRepository.save(fragancia);
        return ResponseEntity.ok(guardada);
    }

    // 📌 Editar fragancia
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PutMapping("/editarFragancia/{id}")
    public ResponseEntity<Fragancia> editar(@PathVariable Long id, @RequestBody Fragancia fragancia) {
        Fragancia existente = fraganciaRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Fragancia no encontrada"));

        existente.setNombre(fragancia.getNombre());
        Fragancia actualizada = fraganciaRepository.save(existente);
        return ResponseEntity.ok(actualizada);
    }

    // 📌 Eliminar fragancia
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @DeleteMapping("/eliminarFragancias/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Fragancia existente = fraganciaRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Fragancia no encontrada"));
        fraganciaRepository.delete(existente);
        return ResponseEntity.ok().build();
    }
}
