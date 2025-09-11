package com.example.AppSaumerios.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OfertaDTO {

    // Solo para creación/actualización
    private Long productoId;            // id del producto al que se aplica la oferta
    private BigDecimal valorDescuento;  // porcentaje o monto
    private String tipoDescuento;       // "PORCENTAJE" o "MONTO"
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean estado;

    // Solo para devolver info completa al cliente
    private Long idOferta;
    private String nombreProducto;
    private String descripcion;
    private BigDecimal precio;

    // ===========================
    // Getters y Setters
    // ===========================
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public BigDecimal getValorDescuento() { return valorDescuento; }
    public void setValorDescuento(BigDecimal valorDescuento) { this.valorDescuento = valorDescuento; }

    public String getTipoDescuento() { return tipoDescuento; }
    public void setTipoDescuento(String tipoDescuento) { this.tipoDescuento = tipoDescuento; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }

    public Long getIdOferta() { return idOferta; }
    public void setIdOferta(Long idOferta) { this.idOferta = idOferta; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
}
