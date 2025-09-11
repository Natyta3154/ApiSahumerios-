package com.example.AppSaumerios.repository;


import com.example.AppSaumerios.entity.Ofertas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfertaRepository extends JpaRepository<Ofertas, Long> {
    // Podés agregar consultas personalizadas si necesitás, por ejemplo:
    // List<Ofertas> findByEstadoTrue();
}
