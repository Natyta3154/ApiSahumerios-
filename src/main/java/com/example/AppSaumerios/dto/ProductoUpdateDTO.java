package com.example.AppSaumerios.dto;



import com.example.AppSaumerios.entity.Productos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductoUpdateDTO {

    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private Boolean activo;
    private String imagenUrl;
    private Long idCategoria;
    private String categoriaNombre;
    private Integer totalIngresado;
    private BigDecimal precioMayorista;
    // Campos de descuento
    private BigDecimal porcentajeDescuento;
    private LocalDate fechaInicioDescuento;
    private LocalDate fechaFinDescuento;

    // Lista de fragancias (nombres)
    private List<String> fragancias = new ArrayList<>();

    // Lista de atributos
    private List<ProductoAtributoDTO> atributos = new ArrayList<>();

    // Clase interna para los atributos
    public static class ProductoAtributoDTO {
        private String nombre;
        private String valor;

        // Getters y setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getValor() { return valor; }
        public void setValor(String valor) { this.valor = valor; }
    }

    // ======================
    // Getters y Setters
    // ======================



    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre;}

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


    public Integer getTotalIngresado() { return totalIngresado; }
    public void setTotalIngresado(Integer totalIngresado) { this.totalIngresado = totalIngresado; }

    public BigDecimal getPrecioMayorista() { return precioMayorista; }
    public void setPrecioMayorista(BigDecimal precioMayorista) { this.precioMayorista = precioMayorista;}

    public List<String> getFragancias() { return fragancias; }
    public void setFragancias(List<String> fragancias) { this.fragancias = fragancias; }

    public List<ProductoAtributoDTO> getAtributos() { return atributos; }
    public void setAtributos(List<ProductoAtributoDTO> atributos) { this.atributos = atributos; }



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
        producto.setTotalIngresado(this.totalIngresado);
        producto.setPrecioMayorista(this.precioMayorista);
        return producto;
    }
}

