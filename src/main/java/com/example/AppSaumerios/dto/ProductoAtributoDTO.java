package com.example.AppSaumerios.dto;

public class ProductoAtributoDTO {
    private Long productoId; // nuevo campo
    private String nombre;
    private String valor;

    public ProductoAtributoDTO() {}

    public ProductoAtributoDTO(String nombre, String valor) {
        this.nombre = nombre;
        this.valor = valor;
    }

    // constructor nuevo para JPQL
    public ProductoAtributoDTO(Long productoId, String nombre, String valor) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.valor = valor;
    }

    // getters y setters
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
}
