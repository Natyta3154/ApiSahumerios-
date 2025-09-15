package com.example.AppSaumerios.dto;

import java.util.List;

public class CrearPedidoResponseDTO {

    private Long pedidoId;
    private String preferenciaId;
    private List<DetallePedidoResponseDTO> detalles; // <-- Cambiado

    // Constructor
    public CrearPedidoResponseDTO(Long pedidoId, String preferenciaId, List<DetallePedidoResponseDTO> detalles) {
        this.pedidoId = pedidoId;
        this.preferenciaId = preferenciaId;
        this.detalles = detalles;
    }

    // Getters y setters
    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getPreferenciaId() {
        return preferenciaId;
    }

    public void setPreferenciaId(String preferenciaId) {
        this.preferenciaId = preferenciaId;
    }

    public List<DetallePedidoResponseDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedidoResponseDTO> detalles) {
        this.detalles = detalles;
    }
}
