package com.example.AppSaumerios.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;



@Entity
@Table(name = "log_notificaciones_pago")
public class LogNotificacionPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_notificacion")
    private String tipoNotificacion;

    @Column(name = "id_notificacion")
    private String idNotificacion;

    @Column(name = "id_pago")
    private String idPago;

    @Column(name = "id_preferencia")
    private String idPreferencia;

    @Column(name = "accion")
    private String accion;

    @Column(name = "estado_pago")
    private String estadoPago;

    @Column(name = "fecha_notificacion")
    private LocalDateTime fechaNotificacion;

    @Column(name = "datos_completos", columnDefinition = "TEXT")
    private String datosCompletos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedidos pedido;

    // Constructores
    public LogNotificacionPago() {
        this.fechaNotificacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipoNotificacion() {
        return tipoNotificacion;
    }

    public void setTipoNotificacion(String tipoNotificacion) {
        this.tipoNotificacion = tipoNotificacion;
    }

    public String getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(String idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public String getIdPago() {
        return idPago;
    }

    public void setIdPago(String idPago) {
        this.idPago = idPago;
    }

    public String getIdPreferencia() {
        return idPreferencia;
    }

    public void setIdPreferencia(String idPreferencia) {
        this.idPreferencia = idPreferencia;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public LocalDateTime getFechaNotificacion() {
        return fechaNotificacion;
    }

    public void setFechaNotificacion(LocalDateTime fechaNotificacion) {
        this.fechaNotificacion = fechaNotificacion;
    }

    public String getDatosCompletos() {
        return datosCompletos;
    }

    public void setDatosCompletos(String datosCompletos) {
        this.datosCompletos = datosCompletos;
    }

    public Pedidos getPedido() {
        return pedido;
    }

    public void setPedido(Pedidos pedido) {
        this.pedido = pedido;
    }
}