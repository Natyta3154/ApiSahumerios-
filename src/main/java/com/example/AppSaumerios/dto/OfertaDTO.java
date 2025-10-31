package com.example.AppSaumerios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data // genera los getter y setter
@NoArgsConstructor //genera el contructor vacio
@AllArgsConstructor // Este es el constructor que necesita JPQL
public class OfertaDTO {


    // Solo para creación/actualización
    private Long productoId;
    private BigDecimal valorDescuento;
    private String tipoDescuento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean estado;

    // Solo para devolver info completa al cliente
    private Long idOferta;
    private String nombreProducto;
    private String descripcion;
    private BigDecimal precio;
}
