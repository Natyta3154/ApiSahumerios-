package com.example.AppSaumerios.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class DetallePedidoDTO {

    @NotNull(message = "El pedidoId no puede ser nulo")
    private Long pedidoId;

    @NotNull(message = "El productoId no puede ser nulo")
    private Long productoId;

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private int cantidad;

    @Positive(message = "El subtotal debe ser positivo")
    private BigDecimal subtotal;

    public DetallePedidoDTO() {}

    public DetallePedidoDTO(Long pedidoId, Long productoId, int cantidad, BigDecimal subtotal) {
        this.pedidoId = pedidoId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }

    // Getters y setters
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}