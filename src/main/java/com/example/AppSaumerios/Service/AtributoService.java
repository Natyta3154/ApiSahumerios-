package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.entity.Atributo;
import com.example.AppSaumerios.repository.AtributoRepository;
import com.example.AppSaumerios.excepciones.RecursoNoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AtributoService {

    @Autowired
    private AtributoRepository atributoRepository;

    // 📌 Listar todos los atributos
    public List<Atributo> listarTodas() {
        return atributoRepository.findAll();
    }

    // 📌 Guardar un atributo
    public Atributo guardar(Atributo atributo) {
        if (atributo.getNombre() == null || atributo.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del atributo no puede estar vacío");
        }
        return atributoRepository.save(atributo);
    }

    // 📌 Actualizar un atributo existente
    public Atributo actualizar(Long id, Atributo atributo) {
        Atributo existente = atributoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Atributo no encontrado con id: " + id));

        if (atributo.getNombre() != null && !atributo.getNombre().isBlank()) {
            existente.setNombre(atributo.getNombre());
        }

       /* if (atributo.getDescripcion() != null) {
            existente.setDescripcion(atributo.getDescripcion());
        }*/

        return atributoRepository.save(existente);
    }

    // 📌 Eliminar un atributo
    public void eliminar(Long id) {
        if (!atributoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Atributo no encontrado con id: " + id);
        }
        atributoRepository.deleteById(id);
    }

    // 📌 Buscar por ID
    public Optional<Atributo> buscarPorId(Long id) {
        return atributoRepository.findById(id);
    }
}
