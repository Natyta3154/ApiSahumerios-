package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.entity.DetallePedido;
import com.example.AppSaumerios.repository.DetallePedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DetallePedidoService {

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private PedidoService pedidoService;

    // Obtener detalles por ID de pedido
    public List<DetallePedido> obtenerDetallesPorPedidoId(Long pedidoId) {
        return detallePedidoRepository.findByPedidoId(pedidoId);
    }

    // Obtener detalle por ID y validar que pertenece al usuario - MÉTODO NUEVO
    public DetallePedido obtenerDetallePorIdYUsuario(Long detalleId, Long usuarioId) {
        Optional<DetallePedido> detalleOpt = detallePedidoRepository.findById(detalleId);

        if (detalleOpt.isPresent()) {
            DetallePedido detalle = detalleOpt.get();
            // Verificar que el pedido pertenece al usuario
            // Esto lanzará excepción si el pedido no pertenece al usuario
            pedidoService.obtenerPedidoPorIdYUsuario(detalle.getPedido().getId(), usuarioId);
            return detalle;
        }
        throw new RuntimeException("Detalle de pedido no encontrado");
    }

    // Obtener detalle solo por ID (sin validación de usuario)
    public DetallePedido obtenerDetallePorId(Long detalleId) {
        return detallePedidoRepository.findById(detalleId)
                .orElseThrow(() -> new RuntimeException("Detalle de pedido no encontrado"));
    }

    // Guardar detalle de pedido
    public DetallePedido guardarDetalle(DetallePedido detallePedido) {
        return detallePedidoRepository.save(detallePedido);
    }

    // Eliminar detalle de pedido
    public void eliminarDetalle(Long detalleId) {
        detallePedidoRepository.deleteById(detalleId);
    }

    // Obtener todos los detalles
    public List<DetallePedido> obtenerTodosDetalles() {
        return detallePedidoRepository.findAll();
    }
}