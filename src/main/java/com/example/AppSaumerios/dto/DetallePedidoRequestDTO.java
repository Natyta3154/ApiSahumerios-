package com.example.AppSaumerios.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class DetallePedidoRequestDTO {

    @NotNull(message = "El productoId no puede ser nulo")
    private Long productoId;

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private int cantidad;

    // Getters y setters
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}