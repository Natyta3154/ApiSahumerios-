package com.example.AppSaumerios.repository;

import com.example.AppSaumerios.entity.Productos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;



@Repository
public interface ProductoRepository extends JpaRepository<Productos, Long > {
        Optional<Productos> findByNombre(String nombre);
        // Devuelve todos los productos con ese nombre
        List<Productos> findAllByNombre(String nombre);

        @Modifying
        @Query("UPDATE Productos p SET p.precio = :precio, p.precioMayorista = :precioMayorista WHERE p.idCategoria = :categoriaId")
        void actualizarPreciosPorCategoria(@Param("categoriaId") Long categoriaId,
                                           @Param("precio") BigDecimal precio,
                                           @Param("precioMayorista") BigDecimal precioMayorista);


}

