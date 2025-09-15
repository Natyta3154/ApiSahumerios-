package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.dto.*;
import com.example.AppSaumerios.entity.Pedidos;
import com.example.AppSaumerios.Service.MercadoPagoService;
import com.example.AppSaumerios.Service.PedidoService;
import com.example.AppSaumerios.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pedidos")
@CrossOrigin(origins = "http://localhost:9002")
public class PedidosController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @Autowired
    private JwtUtil jwtUtil;

    // =========================
    // Crear nuevo pedido
    // =========================
    @PostMapping("/realizarPedido")
    public ResponseEntity<?> crearPedido(@RequestBody CrearPedidoRequestDTO crearPedidoRequestDTO) {
        try {
            Pedidos pedido = pedidoService.crearPedido(crearPedidoRequestDTO);

            List<DetallePedidoResponseDTO> detallesDTO = pedido.getDetalles().stream()
                    .map(pedidoService::convertirADTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(detallesDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =========================
    // Crear nuevo pedido con MercadoPago
    // =========================
    @PostMapping("/realizarPedidoConPago")
    public ResponseEntity<?> crearPedidoConPago(@RequestBody CrearPedidoRequestDTO crearPedidoRequestDTO) {
        try {
            Pedidos pedido = pedidoService.crearPedido(crearPedidoRequestDTO);
            String preferenciaId = mercadoPagoService.crearPreferenciaPago(pedido);

            List<DetallePedidoResponseDTO> detallesDTO = pedido.getDetalles().stream()
                    .map(pedidoService::convertirADTO)
                    .collect(Collectors.toList());

            CrearPedidoResponseDTO response = new CrearPedidoResponseDTO(
                    pedido.getId(),
                    preferenciaId,
                    detallesDTO
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =========================
    // Listar pedidos de un usuario
    // =========================
    @GetMapping
    public ResponseEntity<?> obtenerPedidosUsuario(HttpServletRequest request) {
        try {
            Long usuarioId = obtenerUsuarioIdDesdeToken(request);
            List<Pedidos> pedidos = pedidoService.obtenerPedidosPorUsuario(usuarioId);

            List<CrearPedidoResponseDTO> pedidosDTO = pedidos.stream().map(p -> {
                List<DetallePedidoResponseDTO> detallesDTO = p.getDetalles().stream()
                        .map(pedidoService::convertirADTO)
                        .collect(Collectors.toList());
                return new CrearPedidoResponseDTO(p.getId(), null, detallesDTO);
            }).collect(Collectors.toList());

            return ResponseEntity.ok(pedidosDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =========================
    // Actualizar estado de un pedido (admin)
    // =========================
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id, @RequestParam String estado) {
        try {
            Pedidos pedidoActualizado = pedidoService.actualizarEstado(id, estado);
            return ResponseEntity.ok(pedidoActualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =========================
    // Endpoint de redirección después de pago exitoso
    // =========================
    @GetMapping("/exito")
    public ResponseEntity<String> pagoExitoso(@RequestParam Long pedido_id) {
        try {
            Pedidos pedido = pedidoService.obtenerPedidoPorId(pedido_id);
            return ResponseEntity.ok("Pago exitoso para el pedido: " + pedido_id +
                    ". Estado actual: " + pedido.getEstadoPago());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar pago exitoso: " + e.getMessage());
        }
    }

    // Endpoint para pago fallido
    @GetMapping("/fallo")
    public ResponseEntity<String> pagoFallido(@RequestParam Long pedido_id) {
        try {
            Pedidos pedido = pedidoService.obtenerPedidoPorId(pedido_id);
            return ResponseEntity.ok("Pago fallido para el pedido: " + pedido_id +
                    ". Estado actual: " + pedido.getEstadoPago());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar pago fallido: " + e.getMessage());
        }
    }

    // Endpoint para pago pendiente
    @GetMapping("/pendiente")
    public ResponseEntity<String> pagoPendiente(@RequestParam Long pedido_id) {
        try {
            Pedidos pedido = pedidoService.obtenerPedidoPorId(pedido_id);
            return ResponseEntity.ok("Pago pendiente para el pedido: " + pedido_id +
                    ". Estado actual: " + pedido.getEstadoPago());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar pago pendiente: " + e.getMessage());
        }
    }

    // =========================
    // Webhook de MercadoPago
    // =========================
    @PostMapping("/webhook/mercadopago")
    public ResponseEntity<String> recibirWebhookMercadoPago(@RequestBody(required = false) String rawData,
                                                            @RequestParam Map<String, String> params) {
        try {
            String id = params.get("id");
            String topic = params.get("topic");

            mercadoPagoService.procesarNotificacion(id, topic, rawData);
            return ResponseEntity.ok("Webhook recibido correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error procesando webhook: " + e.getMessage());
        }
    }

    // =========================
    // Obtener estado de pago de un pedido
    // =========================
    @GetMapping("/{pedidoId}/estado-pago")
    public ResponseEntity<?> obtenerEstadoPago(@PathVariable Long pedidoId) {
        try {
            Pedidos pedido = pedidoService.obtenerPedidoPorId(pedidoId);

            EstadoPagoResponseDTO response = new EstadoPagoResponseDTO(
                    pedido.getEstadoPago(),
                    pedido.getPagoId(),
                    pedido.getMetodoPago(),
                    pedido.getFechaActualizacionPago()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error obteniendo estado de pago: " + e.getMessage());
        }
    }

    // =========================
    // Crear preferencia de pago para un pedido existente
    // =========================
    @PostMapping("/{pedidoId}/crear-preferencia-pago")
    public ResponseEntity<?> crearPreferenciaPago(@PathVariable Long pedidoId) {
        try {
            Pedidos pedido = pedidoService.obtenerPedidoPorId(pedidoId);
            String preferenciaId = mercadoPagoService.crearPreferenciaPago(pedido);

            Map<String, String> response = new HashMap<>();
            response.put("preferenciaId", preferenciaId);
            response.put("initPoint", "https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=" + preferenciaId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creando preferencia de pago: " + e.getMessage());
        }
    }

    // =========================
    // Helper: obtener usuario desde JWT
    // =========================
    private Long obtenerUsuarioIdDesdeToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.obtenerIdDesdeToken(token);
        }
        throw new RuntimeException("Token no válido");
    }
}
