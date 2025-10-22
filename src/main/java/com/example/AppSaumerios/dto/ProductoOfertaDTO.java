//muestra las ofertas de productos con precio y descuento → ProductoOfertaDTO


package com.example.AppSaumerios.dto;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ProductoOfertaDTO {
    private Long id;
    private String nombre;
    private String imagenUrl;
    private BigDecimal precioOriginal;
    private BigDecimal precioConDescuento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;


    public ProductoOfertaDTO(Long id, String nombre, BigDecimal precioOriginal, BigDecimal precioConDescuento, String imagenUrl) {
        this.id = id;
        this.nombre = nombre;
        this.precioOriginal = precioOriginal;
        this.precioConDescuento = precioConDescuento;
        this.imagenUrl = imagenUrl;
    }

    public BigDecimal getPrecioFinal() {
        return precioConDescuento != null ? precioConDescuento : precioOriginal;
    }

    public BigDecimal getPrecioDescuento() {
        return (precioConDescuento != null && precioConDescuento.compareTo(precioOriginal) < 0)
                ? precioConDescuento
                : null;
    }

    public boolean tieneDescuento() {
        return getPrecioDescuento() != null;
    }


    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecioOriginal() { return precioOriginal; }
    public BigDecimal getPrecioConDescuento() { return precioConDescuento; }


    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
}
