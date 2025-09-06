package com.example.AppSaumerios.repository;

import com.example.AppSaumerios.entity.Descuento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DescuentoRepository extends JpaRepository<Descuento, Long> {

    // Buscar descuento activo para un producto en una fecha determinada
    @Query("SELECT d FROM Descuento d " +
            "WHERE d.producto.id = :productoId " +
            "AND d.activo = TRUE " +
            "AND :fecha BETWEEN d.fechaInicio AND d.fechaFin")
    Optional<Descuento> findActivoByProducto(@Param("productoId") Long productoId,
                                             @Param("fecha") LocalDate fecha);
}

