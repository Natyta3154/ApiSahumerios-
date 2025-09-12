package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.entity.DetallePedido;
import com.example.AppSaumerios.entity.Pedidos;
import com.example.AppSaumerios.Service.DetallePedidoService;
import com.example.AppSaumerios.Service.PedidoService;
import com.example.AppSaumerios.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/detalle-pedido")
@CrossOrigin(origins = "http://localhost:9002")
public class DetallePedidoController {

    @Autowired
    private DetallePedidoService detallePedidoService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private JwtUtil jwtUtil;

    // Obtener todos los detalles de un pedido (validando que el pedido pertenezca al usuario)
    @GetMapping("/{pedidoId}")
    public ResponseEntity<?> obtenerDetallesPorPedido(@PathVariable Long pedidoId,
                                                      HttpServletRequest request) {
        try {
            // Obtener usuarioId del token JWT
            Long usuarioId = obtenerUsuarioIdDesdeToken(request);

            // Verificar que el pedido pertenece al usuario
            Pedidos pedido = pedidoService.obtenerPedidoPorIdYUsuario(pedidoId, usuarioId);

            // Obtener los detalles del pedido
            List<DetallePedido> detalles = detallePedidoService.obtenerDetallesPorPedidoId(pedidoId);

            return ResponseEntity.ok(detalles);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Obtener un detalle específico por ID - CORREGIDO
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerDetallePorId(@PathVariable Long id,
                                                 HttpServletRequest request) {
        try {
            // Obtener usuarioId del token JWT
            Long usuarioId = obtenerUsuarioIdDesdeToken(request);

            // Obtener el detalle y verificar que pertenece al usuario
            DetallePedido detalle = detallePedidoService.obtenerDetallePorIdYUsuario(id, usuarioId);

            return ResponseEntity.ok(detalle);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Método para obtener el ID del usuario desde el token JWT
    private Long obtenerUsuarioIdDesdeToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.obtenerIdDesdeToken(token);
        }
        throw new RuntimeException("Token no válido");
    }

    // Endpoint adicional: obtener detalle sin validación de usuario (para admin)
    @GetMapping("/admin/{id}")
    public ResponseEntity<?> obtenerDetallePorIdAdmin(@PathVariable Long id) {
        try {
            DetallePedido detalle = detallePedidoService.obtenerDetallePorId(id);
            return ResponseEntity.ok(detalle);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}