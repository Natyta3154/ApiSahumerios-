package com.example.AppSaumerios.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio = BigDecimal.ZERO;
    private BigDecimal precioMayorista = BigDecimal.ZERO;
    private Integer totalIngresado;
    private Integer stock;
    private String imagenUrl;
    private Boolean activo;
    private String categoriaNombre;
    private String mensaje;
    private boolean destacado;
    private List<String> fragancias = new ArrayList<>();
    private List<ProductoAtributoDTO> atributos = new ArrayList<>();

    // Lista de ofertas simplificada para mostrar en frontend
    private List<OfertaSimpleDTO> ofertas = new ArrayList<>();

    // ==============================
    // Getters & Setters
    // ==============================


    public boolean isDestacado() {
        return destacado;
    }

    public void setDestacado(boolean destacado) {
        this.destacado = destacado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public BigDecimal getPrecioMayorista() { return precioMayorista; }
    public void setPrecioMayorista(BigDecimal precioMayorista) { this.precioMayorista = precioMayorista; }

    public Integer getTotalIngresado() { return totalIngresado; }
    public void setTotalIngresado(Integer totalIngresado) { this.totalIngresado = totalIngresado; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }

    public List<String> getFragancias() { return fragancias; }
    public void setFragancias(List<String> fragancias) { this.fragancias = fragancias; }

    public List<ProductoAtributoDTO> getAtributos() { return atributos; }
    public void setAtributos(List<ProductoAtributoDTO> atributos) { this.atributos = atributos; }

    public List<OfertaSimpleDTO> getOfertas() { return ofertas; }
    public void setOfertas(List<OfertaSimpleDTO> ofertas) { this.ofertas = ofertas; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    // ==============================
    // DTO Internos
    // ==============================
    public static class ProductoAtributoDTO {
        private String nombre;
        private String valor;

        public ProductoAtributoDTO() {}
        public ProductoAtributoDTO(String nombre, String valor) {
            this.nombre = nombre;
            this.valor = valor;
        }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getValor() { return valor; }
        public void setValor(String valor) { this.valor = valor; }
    }

    public static class OfertaSimpleDTO {
        private Long idOferta;
        private BigDecimal valorDescuento;
        private String tipoDescuento;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private Boolean estado;
        private BigDecimal precio; // precio original del producto

        public OfertaSimpleDTO() {}
        public Long getIdOferta() { return idOferta; }
        public void setIdOferta(Long idOferta) { this.idOferta = idOferta; }
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
        public BigDecimal getPrecio() { return precio; }
        public void setPrecio(BigDecimal precio) { this.precio = precio; }
    }
}

