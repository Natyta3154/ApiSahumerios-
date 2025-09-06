package com.example.AppSaumerios.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PedidoResponseDTO {

    private Long id;
    private LocalDateTime fecha;
    private BigDecimal total;
    private String estado;
    private Long usuarioId;
    private String usuarioNombre;
    private List<DetallePedidoResponseDTO> detalles; // Cambiado a DetallePedidoResponseDTO

    // Constructores
    public PedidoResponseDTO() {}

    public PedidoResponseDTO(Long id, LocalDateTime fecha, BigDecimal total, String estado,
                             Long usuarioId, String usuarioNombre, List<DetallePedidoResponseDTO> detalles) {
        this.id = id;
        this.fecha = fecha;
        this.total = total;
        this.estado = estado;
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
        this.detalles = detalles;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getEstado() { return estado; } // Corregido: getEstado en lugar de isEstado
    public void setEstado(String estado) { this.estado = estado; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public List<DetallePedidoResponseDTO> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedidoResponseDTO> detalles) { this.detalles = detalles; }
}