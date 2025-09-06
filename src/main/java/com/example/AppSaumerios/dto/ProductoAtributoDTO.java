package com.example.AppSaumerios.dto;



public class ProductoAtributoDTO {
    private String nombre; // nombre del atributo
    private String valor;  // valor del atributo para el producto

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
