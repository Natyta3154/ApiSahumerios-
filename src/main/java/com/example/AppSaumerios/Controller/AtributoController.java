package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.entity.Atributo;
import com.example.AppSaumerios.Service.AtributoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/atributos")
@CrossOrigin(origins = "http://localhost:9002")
public class AtributoController {

    @Autowired
    private AtributoService atributoService;

    // 📌 Listar todos los atributos
    @GetMapping("/listado")
    public List<Atributo> listarTodas() {
        return atributoService.listarTodas();
    }

    // 📌 Agregar un atributo
    @PostMapping("/agregar")
    public ResponseEntity<Atributo> agregarAtributo(@RequestBody Atributo atributo) {
        Atributo guardado = atributoService.guardar(atributo);
        return ResponseEntity.ok(guardado);
    }

    // 📌 Editar un atributo
    @PutMapping("/editar/{id}")
    public ResponseEntity<Atributo> editarAtributo(@PathVariable Long id, @RequestBody Atributo atributo) {
        Atributo actualizada = atributoService.actualizar(id, atributo);
        return ResponseEntity.ok(actualizada);
    }

    // 📌 Eliminar un atributo
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminarAtributo(@PathVariable Long id) {
        atributoService.eliminar(id);
        return ResponseEntity.ok().build();
    }
}
