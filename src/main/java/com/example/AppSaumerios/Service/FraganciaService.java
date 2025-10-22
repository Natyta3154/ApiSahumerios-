package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.entity.Fragancia;
import com.example.AppSaumerios.repository.FraganciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FraganciaService {

    @Autowired
    private FraganciaRepository fraganciaRepository;

    public List<Fragancia> listarTodas() {
        return fraganciaRepository.findAll();
    }

    public Fragancia guardar(Fragancia fragancia) {
        // Evitar duplicados por nombre
        if (fraganciaRepository.existsByNombre(fragancia.getNombre())) {
            throw new RuntimeException("Fragancia ya existe");
        }
        return fraganciaRepository.save(fragancia);
    }

    public Fragancia actualizar(Long id, Fragancia fragancia) {
        Fragancia existente = fraganciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fragancia no encontrada"));

        existente.setNombre(fragancia.getNombre());
        return fraganciaRepository.save(existente);
    }

    public void eliminar(Long id) {
        Fragancia existente = fraganciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fragancia no encontrada"));
        fraganciaRepository.delete(existente);
    }

    public Optional<Fragancia> buscarPorNombre(String nombre) {
        return fraganciaRepository.findByNombre(nombre);
    }
}
