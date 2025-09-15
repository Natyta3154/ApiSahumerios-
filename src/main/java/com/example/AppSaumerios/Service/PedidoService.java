package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.dto.*;
import com.example.AppSaumerios.entity.*;
import com.example.AppSaumerios.repository.DetallePedidoRepository;
import com.example.AppSaumerios.repository.PedidoRepository;
import com.example.AppSaumerios.repository.ProductoRepository;
import com.example.AppSaumerios.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager;





    // =========================
    // Crear pedido con detalles
    // =========================
    public Pedidos crearPedido(CrearPedidoRequestDTO crearPedidoRequestDTO) {
        // Validar y obtener usuario
        Usuarios usuario = usuarioRepository.findById(crearPedidoRequestDTO.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Crear pedido
        Pedidos pedido = new Pedidos();
        pedido.setUsuario(usuario);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setTotal(BigDecimal.ZERO);

        // Calcular total y crear detalles
        BigDecimal total = BigDecimal.ZERO;
        List<DetallePedido> detalles = new ArrayList<>();

        for (DetallePedidoRequestDTO item : crearPedidoRequestDTO.getDetalles()) {
            Productos producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductoId()));

            // Validar stock
            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            // Actualizar stock
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            // Calcular subtotal
            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(subtotal);

            // Crear detalle
            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setSubtotal(subtotal);
            detalle.setPedido(pedido); // Relación con pedido

            detalles.add(detalle);
        }

        // Setear total y lista de detalles
        pedido.setTotal(total);
        pedido.setDetalles(detalles);

        // Guardar pedido con detalles
        Pedidos pedidoGuardado = pedidoRepository.save(pedido);

        // Guardar detalles si no está configurado cascade = ALL
        for (DetallePedido detalle : detalles) {
            detalle.setPedido(pedidoGuardado);
            detallePedidoRepository.save(detalle);
        }

        pedidoGuardado.setDetalles(detalles);
        return pedidoGuardado;
    }

    // =========================
    // Obtener pedido por ID y usuario
    // =========================
    public Pedidos obtenerPedidoPorIdYUsuario(Long pedidoId, Long usuarioId) {
        Pedidos pedido = pedidoRepository.findByIdAndUsuarioId(pedidoId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado para el usuario"));

        // Forzar carga de detalles con JOIN FETCH
        String jpql = "SELECT p FROM Pedidos p LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.producto WHERE p.id = :pedidoId AND p.usuario.id = :usuarioId";
        try {
            Pedidos pedidoCompleto = entityManager.createQuery(jpql, Pedidos.class)
                    .setParameter("pedidoId", pedidoId)
                    .setParameter("usuarioId", usuarioId)
                    .getSingleResult();
            return pedidoCompleto;
        } catch (Exception e) {
            Hibernate.initialize(pedido.getDetalles());
            return pedido;
        }
    }

    // =========================
    // Obtener pedidos por usuario
    // =========================
    public List<Pedidos> obtenerPedidosPorUsuario(Long usuarioId) {
        String jpql = "SELECT DISTINCT p FROM Pedidos p LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.producto WHERE p.usuario.id = :usuarioId ORDER BY p.fecha DESC";
        return entityManager.createQuery(jpql, Pedidos.class)
                .setParameter("usuarioId", usuarioId)
                .getResultList();
    }

    // =========================
    // Obtener todos los pedidos
    // =========================
    public List<Pedidos> obtenerTodosPedidos() {
        return pedidoRepository.findAllByOrderByFechaDesc();
    }

    // =========================
    // Actualizar estado de pedido
    // =========================
    public Pedidos actualizarEstado(Long pedidoId, String estadoStr) {
        Pedidos pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        EstadoPedido estado = EstadoPedido.valueOf(estadoStr.toUpperCase());
        pedido.setEstado(estado);
        return pedidoRepository.save(pedido);
    }

    public Pedidos obtenerPedidoPorId(Long pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
    }

    // =========================
    // Convertir entidad DetallePedido a DTO
    // =========================

    public DetallePedidoResponseDTO convertirADTO(DetallePedido det) {
        DetallePedidoResponseDTO dto = new DetallePedidoResponseDTO();

        dto.setId(det.getId());
        dto.setPedidoId(det.getPedido().getId());
        dto.setProductoId(det.getProducto().getId());
        dto.setProductoNombre(det.getProducto().getNombre());
        dto.setCantidad(det.getCantidad());
        dto.setPrecioUnitario(det.getProducto().getPrecio() != null ? det.getProducto().getPrecio() : BigDecimal.ZERO);
        dto.setSubtotal(det.getProducto().getPrecio() != null
                ? det.getProducto().getPrecio().multiply(BigDecimal.valueOf(det.getCantidad()))
                : BigDecimal.ZERO);
        dto.setImagenUrl(det.getProducto().getImagenUrl());

        return dto;
    }

    // =========================
// Convertir lista de DetallePedido a lista de ResponseDTOs
// =========================
    public List<DetallePedidoResponseDTO> convertirListaADTO(List<DetallePedido> detalles) {
        return detalles.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }




    @Autowired
    private MercadoPagoService mercadoPagoService;

    public Pedidos crearPedidoConPago(CrearPedidoRequestDTO crearPedidoRequestDTO) {
        // Crear el pedido (tu lógica existente)
        Pedidos pedido = crearPedido(crearPedidoRequestDTO);

        // Crear preferencia de pago en MercadoPago
        String preferenciaId = mercadoPagoService.crearPreferenciaPago(pedido);

        return pedido;
    }

    public void actualizarEstadoPago(Long pedidoId, String estadoPago, String pagoId, String metodoPago) {
        Pedidos pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setEstadoPago(estadoPago);
        pedido.setPagoId(pagoId);
        pedido.setMetodoPago(metodoPago);
        pedido.setFechaActualizacionPago(LocalDateTime.now());

        if ("approved".equals(estadoPago)) {
            pedido.setEstado(EstadoPedido.PAGADO);
            pedido.setFechaAprobacionPago(LocalDateTime.now());
        } else if ("rejected".equals(estadoPago)) {
            pedido.setEstado(EstadoPedido.CANCELADO);
        }

        pedidoRepository.save(pedido);
    }



}
