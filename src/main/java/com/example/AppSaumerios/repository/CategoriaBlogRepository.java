package com.example.AppSaumerios.repository;

import com.example.AppSaumerios.entity.CategoriaBlog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaBlogRepository extends JpaRepository<CategoriaBlog, Long> {
    Optional<CategoriaBlog> findByNombre(String nombre);
}

