package com.example.AppSaumerios.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductoUpdateDTO {

    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal precioMayorista;
    private Integer stock;
    private Integer totalIngresado;
    private Boolean activo;
    private String imagenUrl;
    private Boolean destacado;
}
