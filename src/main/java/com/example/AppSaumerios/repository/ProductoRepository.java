package com.example.AppSaumerios.repository;

import com.example.AppSaumerios.dto.ProductoAtributoDTO;
import com.example.AppSaumerios.dto.ProductoResumenDTO;
import com.example.AppSaumerios.entity.Productos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Productos, Long> {

        Optional<Productos> findByNombre(String nombre);

        List<Productos> findAllByNombre(String nombre);

        @Modifying
        @Query("UPDATE Productos p SET p.precio = :precio, p.precioMayorista = :precioMayorista WHERE p.idCategoria = :categoriaId")
        void actualizarPreciosPorCategoria(@Param("categoriaId") Long categoriaId,
                                           @Param("precio") BigDecimal precio,
                                           @Param("precioMayorista") BigDecimal precioMayorista);

        // Top 4 productos activos por categoría ordenados por precio descendente
        List<Productos> findTop4ByActivoTrueAndCategoriaIdOrderByPrecioDesc(Long categoriaId);

        List<Productos> findTop4ByActivoTrueOrderByPrecioDesc();

        @Query("SELECT DISTINCT p FROM Productos p " +
                "LEFT JOIN FETCH p.productoAtributos " +
                "WHERE p.activo = true " +
                "ORDER BY p.precio DESC")
        List<Productos> findDestacadosConRelaciones();

        // ==========================
        // Resumen de productos
        // ==========================
        @Query("SELECT new com.example.AppSaumerios.dto.ProductoResumenDTO(" +
                "p.id, p.nombre, p.precio, p.imagenUrl, p.categoria.nombre, p.destacado, p.descripcion) " +
                "FROM Productos p " +
                "WHERE p.activo = true")
        Page<ProductoResumenDTO> listarResumen(Pageable pageable);


        // ==========================
        // Atributos por producto IDs
        // ==========================
        @Query("SELECT new com.example.AppSaumerios.dto.ProductoAtributoDTO(" +
                "pa.producto.id, a.nombre, pa.valor) " +
                "FROM ProductoAtributo pa " +
                "JOIN pa.atributo a " +
                "WHERE pa.producto.id IN :ids")
        List<ProductoAtributoDTO> listarAtributosPorProductoIds(@Param("ids") List<Long> ids);
}
