package com.example.AppSaumerios.dto;



import com.example.AppSaumerios.entity.Productos;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProductoUpdateDTO {

    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private Boolean activo;
    private String imagenUrl;
    private Long idCategoria;

    // Campos de descuento
    private BigDecimal porcentajeDescuento;
    private LocalDate fechaInicioDescuento;
    private LocalDate fechaFinDescuento;

    // ======================
    // Getters y Setters
    // ======================
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }

    public BigDecimal getPorcentajeDescuento() { return porcentajeDescuento; }
    public void setPorcentajeDescuento(BigDecimal porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }

    public LocalDate getFechaInicioDescuento() { return fechaInicioDescuento; }
    public void setFechaInicioDescuento(LocalDate fechaInicioDescuento) { this.fechaInicioDescuento = fechaInicioDescuento; }

    public LocalDate getFechaFinDescuento() { return fechaFinDescuento; }
    public void setFechaFinDescuento(LocalDate fechaFinDescuento) { this.fechaFinDescuento = fechaFinDescuento; }

    // ======================
    // Conversión a entidad Productos
    // ======================
    public Productos toProductos() {
        Productos producto = new Productos();
        producto.setNombre(this.nombre);
        producto.setDescripcion(this.descripcion);
        producto.setPrecio(this.precio);
        producto.setStock(this.stock != null ? this.stock : 0);
        producto.setActivo(this.activo != null ? this.activo : true);
        producto.setImagenUrl(this.imagenUrl);
        producto.setIdCategoria(this.idCategoria);
        return producto;
    }
}

