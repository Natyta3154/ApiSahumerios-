package com.example.AppSaumerios.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
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
    private Integer stock;

    @Column(name = "categoria_id")
    private Long idCategoria;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private Boolean activo = true;

    // ======================
    // Relación con atributos
    // ======================
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

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<ProductoAtributo> productoAtributos = new HashSet<>();

    // ======================
    // Relación con categoría
    // ======================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Categoria categoria;

    // ======================
    // Relación con fragancias
    // ======================
    @ManyToMany
    @JoinTable(
            name = "producto_fragancia",
            joinColumns = @JoinColumn(name = "producto_id"),
            inverseJoinColumns = @JoinColumn(name = "fragancia_id")
    )
    @JsonManagedReference
    private List<Fragancia> fragancias = new ArrayList<>();

    // ======================
    // Relación con ofertas
    // ======================
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Ofertas> ofertas = new ArrayList<>();


    public void addAtributo(Atributo atributo, String valor) {
        ProductoAtributo pa = new ProductoAtributo(this, atributo, valor);
        this.productoAtributos.add(pa);
    }

    // ======================
    // Constructores
    // ======================
    public Productos() {}

    public Productos(Long id, Integer totalIngresado, String nombre, String descripcion,
                     BigDecimal precio, BigDecimal precioMayorista, Integer stock,
                     Long idCategoria, String imagenUrl, LocalDateTime fechaCreacion, Boolean activo) {
        this.id = id;
        this.totalIngresado = totalIngresado;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.precioMayorista = (precioMayorista != null) ? precioMayorista : BigDecimal.ZERO;
        this.stock = stock;
        this.idCategoria = idCategoria;
        this.imagenUrl = imagenUrl;
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

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Set<ProductoAtributo> getProductoAtributos() { return productoAtributos; }
    public void setProductoAtributos(Set<ProductoAtributo> productoAtributos) { this.productoAtributos = productoAtributos; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public List<Fragancia> getFragancias() { return fragancias; }
    public void setFragancias(List<Fragancia> fragancias) { this.fragancias = fragancias; }

    public List<Ofertas> getOfertas() { return ofertas; }
    public void setOfertas(List<Ofertas> ofertas) { this.ofertas = ofertas; }

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
