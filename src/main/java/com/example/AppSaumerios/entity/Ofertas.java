package com.example.AppSaumerios.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ofertas")
public class Ofertas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_oferta")
    private Long idOferta;

    @Column(name="nombre", nullable = true)
    private String nombreProducto;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "tipo_descuento", nullable = false)
    private String tipoDescuento; // porcentaje o monto fijo

    @Column(name = "valor_descuento", nullable = false)
    private BigDecimal valorDescuento;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "activo")
    private boolean estado;

    @Column(name = "precio", nullable = false)
    private BigDecimal precio;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    @JsonBackReference
    private Productos producto;

    public Ofertas() {}

    public Ofertas(Long idOferta, String nombreProducto, String descripcion, String tipoDescuento,
                   BigDecimal valorDescuento, LocalDate fechaInicio, LocalDate fechaFin,
                   boolean estado, BigDecimal precio) {
        this.idOferta = idOferta;
        this.nombreProducto = nombreProducto;
        this.descripcion = descripcion;
        this.tipoDescuento = tipoDescuento;
        this.valorDescuento = valorDescuento;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.precio = precio;
    }

    public Long getIdOferta() { return idOferta; }
    public void setIdOferta(Long idOferta) { this.idOferta = idOferta; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipoDescuento() { return tipoDescuento; }
    public void setTipoDescuento(String tipoDescuento) { this.tipoDescuento = tipoDescuento; }

    public BigDecimal getValorDescuento() { return valorDescuento; }
    public void setValorDescuento(BigDecimal valorDescuento) { this.valorDescuento = valorDescuento; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public boolean getEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }


    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }


    public Productos getProducto() { return producto; }
    public void setProducto(Productos producto) { this.producto = producto; }

}
