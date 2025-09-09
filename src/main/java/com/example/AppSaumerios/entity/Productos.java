package com.example.AppSaumerios.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "productos")
public class Productos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_ingresado")
    private Integer totalIngresado = 0;


    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(unique = true)
    private String nombre;
    private String descripcion;

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    @Column(name = "precio_mayorista", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioMayorista;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private int stock;

    @Column(name = "categoria_id")
    private Long idCategoria;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private Boolean activo = true;

    @Transient
    public List<Map<String, String>> getAtributos() {
        List<Map<String, String>> list = new ArrayList<>();
        for (ProductoAtributo pa : this.productoAtributos) {
            Map<String, String> map = new HashMap<>();
            map.put("nombre", pa.getAtributo().getNombre());
            map.put("valor", pa.getValor());
            list.add(map);
        }
        return list;
    }

    // Luego
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<ProductoAtributo> productoAtributos = new HashSet<>();



    // Relación con descuentos
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Descuento> descuentos = new ArrayList<>();

    // Relación con categoría
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Categoria categoria;

    @ManyToMany
    @JoinTable(
            name = "producto_fragancia",
            joinColumns = @JoinColumn(name = "producto_id"),
            inverseJoinColumns = @JoinColumn(name = "fragancia_id")
    )
    @JsonManagedReference
    private List<Fragancia> fragancias = new ArrayList<>();

    // ======================
    // Constructores
    // ======================
    public Productos() {}

    public Productos(Long id, Integer totalIngresado, String nombre, String descripcion,
                     BigDecimal precio, BigDecimal precioMayorista, int stock,
                     Long idCategoria, String imagenurl,
                     LocalDateTime fechaCreacion, Boolean activo) {
        this.id = id;
        this.totalIngresado = totalIngresado;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.precioMayorista = (precioMayorista != null) ? precioMayorista : BigDecimal.ZERO;
        this.stock = stock;
        this.idCategoria = idCategoria;
        this.imagenUrl = imagenurl;
        this.fechaCreacion = fechaCreacion;
        this.activo = activo;
    }


    // ======================
    // Getters y Setters
    // ======================
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
    public void setPrecioMayorista(BigDecimal precioMayorista) {
        this.precioMayorista = (precioMayorista != null) ? precioMayorista : BigDecimal.ZERO;
    }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenurl) { this.imagenUrl = imagenurl; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Set<ProductoAtributo> getProductoAtributos() { return productoAtributos; }
    public void setProductoAtributos(Set<ProductoAtributo> productoAtributos) { this.productoAtributos = productoAtributos; }

    public List<Descuento> getDescuentos() { return descuentos; }
    public void setDescuentos(List<Descuento> descuentos) { this.descuentos = descuentos; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public List<Fragancia> getFragancias() { return fragancias; }
    public void setFragancias(List<Fragancia> fragancias) { this.fragancias = fragancias; }

    // ======================
    // Métodos auxiliares
    // ======================
    public void addAtributo(Atributo atributo, String valor) {
        ProductoAtributo pa = new ProductoAtributo(this, atributo, valor);
        productoAtributos.add(pa);
    }

    public void removeAtributo(Atributo atributo) {
        productoAtributos.removeIf(pa -> pa.getAtributo().equals(atributo));
    }

    // Obtener el descuento activo
    public Descuento getDescuentoActivo() {
        return descuentos.stream()
                .filter(Descuento::estaVigente)
                .findFirst()
                .orElse(null);
    }

    // Datos calculados para JSON
    public BigDecimal getPorcentajeDescuento() {
        Descuento d = getDescuentoActivo();
        return d != null ? d.getPorcentaje() : null;
    }

    public LocalDate getFechaInicioDescuento() {
        Descuento d = getDescuentoActivo();
        return d != null ? d.getFechaInicio() : null;
    }

    public LocalDate getFechaFinDescuento() {
        Descuento d = getDescuentoActivo();
        return d != null ? d.getFechaFin() : null;
    }

    public BigDecimal getPrecioFinal() {
        Descuento d = getDescuentoActivo();
        if (d != null) {
            BigDecimal descuento = precio.multiply(d.getPorcentaje()).divide(new BigDecimal("100"));
            return precio.subtract(descuento);
        }
        return precio;
    }

    public String getCategoriaNombre() {
        return categoria != null ? categoria.getNombre() : null;
    }

    @Override
    public String toString() {
        return "Productos{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", stock=" + stock +
                '}';
    }
}
