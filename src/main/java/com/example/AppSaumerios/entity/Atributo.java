package com.example.AppSaumerios.entity;

import jakarta.persistence.*;
import java.io.Serializable;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Entity
@Table(name = "atributos")
public class Atributo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //  Campo nombre
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

   // private String descripcion;

    // Constructor vacío requerido por JPA
    public Atributo() {}

    //  Constructor útil
    public Atributo(String nombre) {
        this.nombre = nombre;
        //this.descripcion = descripcion;
    }

    //  Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

  //  public String getDescripcion() {
       // return descripcion;
    //}

   // public void setDescripcion(String descripcion) {
       // this.descripcion = descripcion;
   // }

    @Override
    public String toString() {
        return "Atributo{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
               // ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
