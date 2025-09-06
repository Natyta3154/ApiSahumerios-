package com.example.AppSaumerios.repository;
import java.util.Optional;
import com.example.AppSaumerios.entity.Atributo;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AtributoRepository extends JpaRepository<Atributo, Long> {
    Optional<Atributo> findByNombre(String nombre);
}