package com.example.AppSaumerios.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.security.PublicKey;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class ProductoDTO {

        private Long id;
        private String nombre;
        private String descripcion;
    @DecimalMin(value = "0.0", message = "El precio debe ser mayor o igual a 0")
    private BigDecimal precio = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "El precio mayorista debe ser mayor o igual a 0")
    @JsonProperty("precioMayorista")
    private BigDecimal precioMayorista = BigDecimal.ZERO;

    @Min(value = 0, message = "El total ingresado no puede ser negativo")
    @JsonProperty("totalIngresado")
        private Integer totalIngresado;

        private Integer stock;


        @JsonProperty("imagenurl")
        private String imagenUrl;


        private Boolean activo;
        private String categoriaNombre;
        private String mensaje;
        private List<String> fragancias = new ArrayList<>();

        // Info de descuento vigente
        private BigDecimal porcentajeDescuento;
        private LocalDate fechaInicioDescuento;
        private LocalDate fechaFinDescuento;

        // Precio final ya calculado
        private BigDecimal precioFinal;

        // Atributos generales
        private List<ProductoAtributoDTO> atributos = new ArrayList<>();



        // ================================
        // Getters & Setters
        // ================================

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Integer getTotalIngresado() { return totalIngresado; }
        public void setTotalIngresado(Integer totalIngresado) { this.totalIngresado = totalIngresado; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

        public BigDecimal getPrecio() { return precio; }
        public void setPrecio(BigDecimal precio) { this.precio = precio; }


        public BigDecimal getPrecioMayorista() { return precioMayorista; }
        public void setPrecioMayorista(BigDecimal precioMayorista) {this.precioMayorista = precioMayorista;}


        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }

        public String getImagenUrl() { return imagenUrl; }
        public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

        public Boolean getActivo() { return activo; }
        public void setActivo(Boolean activo) { this.activo = activo; }

        public String getCategoriaNombre() { return categoriaNombre; }
        public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }

        public BigDecimal getPorcentajeDescuento() { return porcentajeDescuento; }
        public void setPorcentajeDescuento(BigDecimal porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }

        public LocalDate getFechaInicioDescuento() { return fechaInicioDescuento; }
        public void setFechaInicioDescuento(LocalDate fechaInicioDescuento) { this.fechaInicioDescuento = fechaInicioDescuento; }

        public LocalDate getFechaFinDescuento() { return fechaFinDescuento; }
        public void setFechaFinDescuento(LocalDate fechaFinDescuento) { this.fechaFinDescuento = fechaFinDescuento; }

        public BigDecimal getPrecioFinal() { return precioFinal; }
        public void setPrecioFinal(BigDecimal precioFinal) { this.precioFinal = precioFinal; }

        public List<ProductoAtributoDTO> getAtributos() { return atributos; }
        public void setAtributos(List<ProductoAtributoDTO> atributos) { this.atributos = atributos; }

        public List<String> getFragancias() { return fragancias; }
        public void setFragancias(List<String> fragancias) { this.fragancias = fragancias; }

       public String getMensaje() { return mensaje; }
       public void setMensaje(String mensaje) { this.mensaje = mensaje; }


        // ================================
        // DTO Interno para los atributos
        // ================================
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

    }




