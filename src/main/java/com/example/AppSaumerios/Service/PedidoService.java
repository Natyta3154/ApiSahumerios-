package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.dto.*;
import com.example.AppSaumerios.entity.*;
import com.example.AppSaumerios.repository.DetallePedidoRepository;
import com.example.AppSaumerios.repository.PedidoRepository;
import com.example.AppSaumerios.repository.ProductoRepository;
import com.example.AppSaumerios.repository.UsuarioRepository;
import com.example.AppSaumerios.Service.PedidoService;
import com.example.AppSaumerios.util.JwtUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
            detalle.setPedido(pedido); // 👈 importante: relaciono el detalle con el pedido

            detalles.add(detalle);
        }

        // Setear total y lista de detalles
        pedido.setTotal(total);
        pedido.setDetalles(detalles); // 👈 clave: el pedido ahora "conoce" sus detalles

        // Guardar pedido con sus detalles
        Pedidos pedidoGuardado = pedidoRepository.save(pedido);

        // ⚠️ Si tu entidad Pedidos tiene cascade = CascadeType.ALL,
        // no hace falta este bucle, ya guarda todo junto.
        for (DetallePedido detalle : detalles) {
            detalle.setPedido(pedidoGuardado);
            detallePedidoRepository.save(detalle);
        }

        // Refrescar detalles para devolverlos en el JSON
        pedidoGuardado.setDetalles(detalles);

        return pedidoGuardado;
    }


    public Pedidos obtenerPedidoPorIdYUsuario(Long pedidoId, Long usuarioId) {
        Pedidos pedido = pedidoRepository.findByIdAndUsuarioId(pedidoId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado para el usuario"));

        // ✅ Forzar carga de detalles con JOIN FETCH (más eficiente)
        String jpql = "SELECT p FROM Pedidos p LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.producto WHERE p.id = :pedidoId AND p.usuario.id = :usuarioId";

        try {
           ;
            Pedidos pedidoCompleto = entityManager.createQuery(jpql, Pedidos.class)
                    .setParameter("pedidoId", pedidoId)
                    .setParameter("usuarioId", usuarioId)
                    .getSingleResult();
            return pedidoCompleto;
        } catch (Exception e) {
            // Fallback al método original
            Hibernate.initialize(pedido.getDetalles());
            return pedido;
        }
    }

    public List<Pedidos> obtenerPedidosPorUsuario(Long usuarioId) {
        // ✅ Usar JOIN FETCH para cargar todo en una query
        String jpql = "SELECT DISTINCT p FROM Pedidos p LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.producto WHERE p.usuario.id = :usuarioId ORDER BY p.fecha DESC";

        return entityManager.createQuery(jpql, Pedidos.class)
                .setParameter("usuarioId", usuarioId)
                .getResultList();
    }

    public List<Pedidos> obtenerTodosPedidos() {
        return pedidoRepository.findAllByOrderByFechaDesc();
    }
    public Pedidos actualizarEstado(Long pedidoId, String estadoStr) {
        Pedidos pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Convertir String a Enum
        EstadoPedido estado = EstadoPedido.valueOf(estadoStr.toUpperCase());
        pedido.setEstado(estado); // ✅ Correcto
        return pedidoRepository.save(pedido);
    }

    public Pedidos obtenerPedidoPorId(Long pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
    }



}