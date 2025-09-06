package com.example.AppSaumerios.repository;



import com.example.AppSaumerios.entity.Fragancia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FraganciaRepository extends JpaRepository<Fragancia, Long> {

    // Buscar fragancia por nombre
    Optional<Fragancia> findByNombre(String nombre);

    // Si querés, también podés agregar:
    boolean existsByNombre(String nombre);
}
