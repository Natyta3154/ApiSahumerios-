package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.dto.DetallePedidoRequestDTO;
import com.example.AppSaumerios.entity.DetallePedido;
import com.example.AppSaumerios.entity.Productos;
import com.example.AppSaumerios.repository.DetallePedidoRepository;
import com.example.AppSaumerios.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DetallePedidoService {

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    // Crear detalle de pedido
    public DetallePedido crearDetalle(Long usuarioId, DetallePedidoRequestDTO dto) {
        Productos producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + dto.getProductoId()));

        if (producto.getStock() < dto.getCantidad()) {
            throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
        }

        // Descontar stock
        producto.setStock(producto.getStock() - dto.getCantidad());
        productoRepository.save(producto);

        DetallePedido detalle = new DetallePedido();
        detalle.setProducto(producto);
        detalle.setCantidad(dto.getCantidad());
        detalle.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(dto.getCantidad())));

        return detallePedidoRepository.save(detalle);
    }

    // Actualizar detalle de pedido
    public DetallePedido actualizarDetalle(DetallePedido detalle, DetallePedidoRequestDTO dto) {
        Productos producto = detalle.getProducto();

        int diferencia = dto.getCantidad() - detalle.getCantidad();

        if (diferencia > 0 && producto.getStock() < diferencia) {
            throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
        }

        // Ajustar stock
        producto.setStock(producto.getStock() - diferencia);
        productoRepository.save(producto);

        // Actualizar detalle
        detalle.setCantidad(dto.getCantidad());
        detalle.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(dto.getCantidad())));

        return detallePedidoRepository.save(detalle);
    }

    // Eliminar detalle de pedido
    public void eliminarDetalle(DetallePedido detalle) {
        Productos producto = detalle.getProducto();
        producto.setStock(producto.getStock() + detalle.getCantidad());
        productoRepository.save(producto);

        detallePedidoRepository.delete(detalle);
    }

    // Obtener detalle por ID (usuario)
    public DetallePedido obtenerDetallePorIdYUsuario(Long detalleId, Long usuarioId) {
        return detallePedidoRepository.findById(detalleId)
                .filter(d -> d.getPedido().getUsuario().getId().equals(usuarioId))
                .orElse(null);
    }

    // Obtener detalle por ID (admin)
    public DetallePedido obtenerDetallePorId(Long detalleId) {
        return detallePedidoRepository.findById(detalleId).orElse(null);
    }

    // Obtener detalles de un pedido
    public List<DetallePedido> obtenerDetallesPorPedidoId(Long pedidoId) {
        return detallePedidoRepository.findByPedidoId(pedidoId);
    }
}
