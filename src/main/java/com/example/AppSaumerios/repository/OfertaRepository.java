package com.example.AppSaumerios.repository;

import com.example.AppSaumerios.dto.OfertaDTO;
import com.example.AppSaumerios.entity.Ofertas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OfertaRepository extends JpaRepository<Ofertas, Long> {

    @Query("""
    SELECT new com.example.AppSaumerios.dto.OfertaDTO(
        o.producto.id,
        o.valorDescuento,
        o.tipoDescuento,
        o.fechaInicio,
        o.fechaFin,
        o.estado,
        o.idOferta,
        o.nombreProducto,
        o.descripcion,
        o.precio
    )
    FROM Ofertas o
    WHERE o.estado = true
    ORDER BY o.fechaInicio DESC
""")
    List<OfertaDTO> findActiveOffersForCarousel();
}


