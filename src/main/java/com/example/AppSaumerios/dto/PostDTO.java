package com.example.AppSaumerios.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private String contenido;
    private String imagenUrl;
    private CategoriaBlogDTO categoria;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}

