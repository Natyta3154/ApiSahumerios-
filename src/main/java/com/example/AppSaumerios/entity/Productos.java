package com.example.AppSaumerios.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.apache.commons.collections4.list.LazyList;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "productos")
public class Productos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_ingresado")
    private Integer totalIngresado = 0;

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(unique = true)
    private String nombre;

    private String descripcion;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal precio;

    @Column(name = "precio_mayorista", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioMayorista;

    @Min(0)
    private Integer stock;

    @Column(name = "categoria_id")
    private Long idCategoria;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Boolean destacado = false;


    // ======================
    // RELACIONES
    // ======================

    // Atributos del producto
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ProductoAtributo> productoAtributos = new ArrayList<>();

    // Categoría
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", insertable = false, updatable = false)
    private Categoria categoria;

    // Fragancias
    @ManyToMany
    @JoinTable(
            name = "producto_fragancia",
            joinColumns = @JoinColumn(name = "producto_id"),
            inverseJoinColumns = @JoinColumn(name = "fragancia_id")
    )

    @OrderBy("nombre ASC")
    @JsonManagedReference
    @Fetch(FetchMode.SUBSELECT)
    private List<Fragancia> fragancias = new ArrayList<>();

    // Ofertas
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Fetch(FetchMode.SUBSELECT)
    private List<Ofertas> ofertas = new ArrayList<>();

    // DetallePedidos
    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
    private List<DetallePedido> detallePedidos = new ArrayList<>();

    // ======================
    // MÉTODOS AUXILIARES
    // ======================

    @Transient
    public List<Map<String, String>> getAtributos() {
        return productoAtributos.stream()
                .map(pa -> Map.of(
                        "nombre", pa.getAtributo().getNombre(),
                        "valor", pa.getValor()
                ))
                .collect(Collectors.toList());
    }

    public void addAtributo(Atributo atributo, String valor) {
        ProductoAtributo pa = new ProductoAtributo(this, atributo, valor);
        this.productoAtributos.add(pa);
    }

    public String getCategoriaNombre() {
        return categoria != null ? categoria.getNombre() : null;
    }

    // ======================
    // CONSTRUCTORES
    // ======================
    public Productos() {}

    public Productos(Long id, String nombre, String descripcion, BigDecimal precio,
                     BigDecimal precioMayorista, Integer stock, Long idCategoria,
                     String imagenUrl, Boolean activo, Integer totalIngresado) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.precioMayorista = precioMayorista != null ? precioMayorista : BigDecimal.ZERO;
        this.stock = stock;
        this.idCategoria = idCategoria;
        this.imagenUrl = imagenUrl;
        this.activo = activo != null ? activo : true;
        this.totalIngresado = totalIngresado != null ? totalIngresado : 0;
        this.fechaCreacion = LocalDateTime.now();
    }

    // ======================
    // GETTERS Y SETTERS
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
        this.precioMayorista = precioMayorista != null ? precioMayorista : BigDecimal.ZERO;
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

    public List<ProductoAtributo> getProductoAtributos() { return productoAtributos; }
    public void setProductoAtributos(List<ProductoAtributo> productoAtributos) { this.productoAtributos = productoAtributos; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public List<Fragancia> getFragancias() { return fragancias; }
    public void setFragancias(List<Fragancia> fragancias) { this.fragancias = fragancias; }

    public List<Ofertas> getOfertas() { return ofertas; }
    public void setOfertas(List<Ofertas> ofertas) { this.ofertas = ofertas; }

    public List<DetallePedido> getDetallePedidos() { return detallePedidos; }
    public void setDetallePedidos(List<DetallePedido> detallePedidos) { this.detallePedidos = detallePedidos; }

    public Boolean getDestacado() {
        return destacado;
    }

    public void setDestacado(Boolean destacado) {
        this.destacado = destacado;
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
