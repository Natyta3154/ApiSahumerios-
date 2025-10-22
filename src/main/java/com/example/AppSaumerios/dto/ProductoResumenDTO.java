package com.example.AppSaumerios.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ProductoResumenDTO {

    private Long id;
    private String nombre;
    private BigDecimal precio;
    private String imagenUrl;
    private String descripcion;
    private String categoriaNombre;
    private boolean destacado;
    public ProductoResumenDTO() {
        // Constructor vacío necesario para frameworks como Jackson
    }

    public ProductoResumenDTO(Long id, String nombre, BigDecimal precio, String imagenUrl, String categoriaNombre, boolean destacado, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
        this.categoriaNombre = categoriaNombre;
        this.destacado = destacado;
    }

}

