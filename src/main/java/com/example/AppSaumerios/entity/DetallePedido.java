package com.example.AppSaumerios.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    // Relación ManyToOne con Pedidos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    @JsonBackReference // ✅ Evita recursividad infinita
    private Pedidos pedido;

    // Relación ManyToOne con Producto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // ✅ Evita problemas de serialización
    private Productos producto;

    private int cantidad;
    private BigDecimal subtotal;

    public DetallePedido() {}

    public DetallePedido(Pedidos pedido, Productos producto, int cantidad, BigDecimal subtotal) {
        this.pedido = pedido;
        this.producto = producto;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Pedidos getPedido() {
        return pedido;
    }
    public void setPedido(Pedidos pedido) {
        this.pedido = pedido;
    }

    public Productos getProducto() {
        return producto;
    }
    public void setProducto(Productos producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
