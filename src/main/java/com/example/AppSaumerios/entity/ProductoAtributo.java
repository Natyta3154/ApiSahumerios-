package com.example.AppSaumerios.entity;

import com.example.AppSaumerios.entity.Atributo;
import com.example.AppSaumerios.entity.Productos;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "producto_atributos")
public class ProductoAtributo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Productos producto;

    @ManyToOne
    @JoinColumn(name = "atributo_id", nullable = false)
    private Atributo atributo;

    private String valor; // Ejemplo: "Lavanda", "Rojo", "Grande"

    // Constructor vacío
    public ProductoAtributo() {}

    // Constructor con parámetros
    public ProductoAtributo(Productos producto, Atributo atributo, String valor) {
        this.producto = producto;
        this.atributo = atributo;
        this.valor = valor;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Productos getProducto() {
        return producto;
    }

    public void setProducto(Productos producto) {
        this.producto = producto;
    }

    public Atributo getAtributo() {
        return atributo;
    }

    public void setAtributo(Atributo atributo) {
        this.atributo = atributo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "ProductoAtributo{" +
                "id=" + id +
                ", producto=" + (producto != null ? producto.getNombre() : null) +
                ", atributo=" + (atributo != null ? atributo.getNombre() : null) +
                ", valor='" + valor + '\'' +
                '}';
    }
}
