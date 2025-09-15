package com.example.AppSaumerios.dto;

import java.time.LocalDateTime;
import java.util.Date;

public class EstadoPagoResponseDTO {
    private String estadoPago;
    private String pagoId;
    private String metodoPago;
    private LocalDateTime fechaActualizacion;

    public EstadoPagoResponseDTO(String estadoPago, String pagoId, String metodoPago, LocalDateTime fechaActualizacion) {
        this.estadoPago = estadoPago;
        this.pagoId = pagoId;
        this.metodoPago = metodoPago;
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public String getPagoId() {
        return pagoId;
    }

    public void setPagoId(String pagoId) {
        this.pagoId = pagoId;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
