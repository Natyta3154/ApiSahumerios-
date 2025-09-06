package com.example.AppSaumerios.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CrearPedidoDTO {

    @NotNull(message = "El usuarioId no puede ser nulo")
    private Long usuarioId;

    @NotEmpty(message = "Debe incluir al menos un detalle de pedido")
    @Valid
    private List<DetallePedidoDTO> detalles;

    // Getters y setters
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public List<DetallePedidoDTO> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedidoDTO> detalles) { this.detalles = detalles; }
}