package com.example.AppSaumerios.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedidos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuarios usuario;


    private LocalDateTime fecha;
    private BigDecimal total;

    //  Usar String para el ENUM
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('PENDIENTE', 'PAGADO', 'ENVIADO', 'CANCELADO')")
    private EstadoPedido estado;


    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("pedido") // ✅ Evita recursividad infinita
    private List<DetallePedido> detalles = new ArrayList<>();

    // *********** integración con MercadoPago *************
    @Column(name = "preferencia_id")
    private String preferenciaId;

    @Column(name = "pago_id")
    private String pagoId;

    @Column(name = "estado_pago")
    private String estadoPago;

    @Column(name = "fecha_actualizacion_pago")
    private LocalDateTime fechaActualizacionPago;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @Column(name = "fecha_aprobacion_pago")
    private LocalDateTime fechaAprobacionPago;




    public Pedidos() {}

    public Pedidos(Long id, Usuarios usuario, LocalDateTime fecha, BigDecimal total, EstadoPedido estado) {
        this.id = id;
        this.usuario = usuario;
        this.fecha = fecha;
        this.total = total;
        this.estado = estado;  // ✅ Ahora recibe EstadoPedido en lugar de String
    }

    //getter y setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuarios getUsuario() { return usuario; }
    public void setUsuario(Usuarios usuario) { this.usuario = usuario; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

   // public String isEstado() { return estado; }
    //public void setEstado(String estado) { this.estado = estado; }

    public List<DetallePedido> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedido> detalles) { this.detalles = detalles; }


    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }


    // Getters y Setters INTEGRACION MERCADOPAGO
    public String getPreferenciaId() { return preferenciaId; }
    public void setPreferenciaId(String preferenciaId) { this.preferenciaId = preferenciaId; }

    public String getPagoId() { return pagoId; }
    public void setPagoId(String pagoId) { this.pagoId = pagoId; }

    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }

    public LocalDateTime getFechaActualizacionPago() { return fechaActualizacionPago; }
    public void setFechaActualizacionPago(LocalDateTime fechaActualizacionPago) { this.fechaActualizacionPago = fechaActualizacionPago; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public LocalDateTime getFechaAprobacionPago() { return fechaAprobacionPago; }
    public void setFechaAprobacionPago(LocalDateTime fechaAprobacionPago) { this.fechaAprobacionPago = fechaAprobacionPago; }
}



