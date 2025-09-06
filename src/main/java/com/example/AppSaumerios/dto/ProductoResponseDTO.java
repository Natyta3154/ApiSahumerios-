package com.example.AppSaumerios.dto;

public class ProductoResponseDTO {
    private String mensaje;
    private ProductoDTO producto;

    // Constructor
    public ProductoResponseDTO(String mensaje, ProductoDTO producto) {
        this.mensaje = mensaje;
        this.producto = producto;
    }

    // Getters y setters
    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public ProductoDTO getProducto() {
        return producto;
    }

    public void setProducto(ProductoDTO producto) {
        this.producto = producto;
    }
}
