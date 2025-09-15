package com.example.AppSaumerios.repository;

import com.example.AppSaumerios.entity.Pedidos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedidos, Long> {

    // Métodos existentes
    Optional<Pedidos> findByIdAndUsuarioId(Long id, Long usuarioId);
    List<Pedidos> findByUsuarioId(Long usuarioId);
    List<Pedidos> findAllByOrderByFechaDesc();

    // =========================
    // NUEVOS MÉTODOS PARA INTEGRACIÓN CON MERCADO PAGO
    // =========================

    // Buscar pedido por ID de preferencia de MercadoPago
    Optional<Pedidos> findByPreferenciaId(String preferenciaId);

    // Buscar pedido por ID de pago de MercadoPago
    Optional<Pedidos> findByPagoId(String pagoId);

    // Buscar pedidos por estado de pago
    List<Pedidos> findByEstadoPago(String estadoPago);

    // Buscar pedidos por estado de pago y usuario
    List<Pedidos> findByEstadoPagoAndUsuarioId(String estadoPago, Long usuarioId);

    // Buscar pedidos pendientes de pago (creados hace más de X tiempo)
    @Query("SELECT p FROM Pedidos p WHERE p.estadoPago = 'PENDIENTE' AND p.fecha < :fechaLimite")
    List<Pedidos> findPedidosPendientesExpirados(@Param("fechaLimite") LocalDateTime fechaLimite);

    // Buscar pedidos por rango de fechas de actualización de pago
    List<Pedidos> findByFechaActualizacionPagoBetween(LocalDateTime inicio, LocalDateTime fin);

    // Buscar pedidos por método de pago
    List<Pedidos> findByMetodoPago(String metodoPago);

    // Contar pedidos por estado de pago
    Long countByEstadoPago(String estadoPago);

    // Buscar pedidos por estado de pago y método de pago
    List<Pedidos> findByEstadoPagoAndMetodoPago(String estadoPago, String metodoPago);
}