package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.dto.*;
import com.example.AppSaumerios.entity.Pedidos;
import com.example.AppSaumerios.entity.Usuarios;
import com.example.AppSaumerios.Service.MercadoPagoService;
import com.example.AppSaumerios.Service.PedidoService;
import com.example.AppSaumerios.Service.UsuarioService;
import com.example.AppSaumerios.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pedidos")
@CrossOrigin(
        origins = {
                "http://localhost:9002",
                "https://front-sahumerios-2.vercel.app",
                "https://app-sahumerio3.vercel.app" // tu dominio de producción
        },
        allowCredentials = "true"
)
public class PedidosController {


    @Value("${frontend.url.${spring.profiles.active}}")
    private String frontendUrl;


    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioService usuarioService;

    // =========================
    // Crear nuevo pedido (usuario logueado)
    // =========================
    @PostMapping("/realizarPedido")
    public ResponseEntity<?> crearPedido(@RequestBody CrearPedidoRequestDTO crearPedidoRequestDTO,
                                         HttpServletRequest request) {
        try {
            Usuarios usuario = obtenerUsuarioDesdeToken(request);
            crearPedidoRequestDTO.setUsuarioId(usuario.getId()); // asignar ID real
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
    // Crear pedido con pago (usuario logueado)
    // =========================
    @PostMapping("/realizarPedidoConPago")
    public ResponseEntity<?> crearPedidoConPago(@RequestBody CrearPedidoRequestDTO crearPedidoRequestDTO,
                                                HttpServletRequest request) {
        try {
            Usuarios usuario = obtenerUsuarioDesdeToken(request);
            crearPedidoRequestDTO.setUsuarioId(usuario.getId());
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
    // Obtener pedidos del usuario logueado
    // =========================
    @GetMapping
    public ResponseEntity<?> obtenerPedidosUsuario(HttpServletRequest request) {
        try {
            Usuarios usuario = obtenerUsuarioDesdeToken(request);
            List<Pedidos> pedidos = pedidoService.obtenerPedidosPorUsuario(usuario.getId());

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
    // Actualizar estado de un pedido (solo admin)
    // =========================
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id,
                                              @RequestParam String estado,
                                              Authentication authentication) {
        // Solo admins pueden cambiar estados
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!esAdmin) {
            return ResponseEntity.status(403).body("No tienes permisos para actualizar el estado");
        }

        try {
            Pedidos pedidoActualizado = pedidoService.actualizarEstado(id, estado);
            return ResponseEntity.ok(pedidoActualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =========================
    // Webhook y pagos (igual)
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
    private Usuarios obtenerUsuarioDesdeToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long usuarioId = jwtUtil.obtenerIdDesdeToken(token);
            return usuarioService.obtenerUsuarioPorId(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        }
        throw new RuntimeException("Token no válido");
    }
}
