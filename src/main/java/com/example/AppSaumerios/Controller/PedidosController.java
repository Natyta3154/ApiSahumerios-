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
                "https://app-aroman.vercel.app"  // tu dominio de producción
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

/*Enpoint de Prueba test
    @GetMapping("/test-preferencia/{pedidoId}")
    public ResponseEntity<?> testPreferencia(@PathVariable Long pedidoId) {
        try {
            Pedidos pedido = pedidoService.obtenerPedidoPorId(pedidoId);
            // Usamos el mismo método del service, pero solo para obtener URLs
            String successUrl = frontendUrl + "/checkout/exito?pedido_id=" + pedido.getId();
            String failureUrl = frontendUrl + "/checkout/fallo?pedido_id=" + pedido.getId();
            String pendingUrl = frontendUrl + "/checkout/pendiente?pedido_id=" + pedido.getId();

            Map<String, String> urls = new HashMap<>();
            urls.put("success", successUrl);
            urls.put("failure", failureUrl);
            urls.put("pending", pendingUrl);

            return ResponseEntity.ok(urls);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }*/


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

            // 🔹 Este método ahora debe devolver la URL completa de pago (init_point)
            String initPoint = mercadoPagoService.crearPreferenciaPago(pedido);

            List<DetallePedidoResponseDTO> detallesDTO = pedido.getDetalles().stream()
                    .map(pedidoService::convertirADTO)
                    .collect(Collectors.toList());

            // 🔹 devolvemos la URL en un JSON simple y claro
            Map<String, Object> response = new HashMap<>();
            response.put("pedidoId", pedido.getId());
            response.put("init_point", initPoint);
            response.put("detalles", detallesDTO);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
            String initPoint = mercadoPagoService.crearPreferenciaPago(pedido);

            Map<String, Object> response = new HashMap<>();
            response.put("init_point", initPoint);
            response.put("pedido_id", pedido.getId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creando preferencia de pago: " + e.getMessage());
        }
    }


    // =========================
    // Helper: obtener usuario desde JWT
    // =========================
    private Usuarios obtenerUsuarioDesdeToken(HttpServletRequest request) {
        // Revisar header Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long usuarioId = jwtUtil.obtenerIdDesdeToken(token);
            return usuarioService.obtenerUsuarioPorId(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        }

        // Revisar cookies
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("token")) {
                    String token = cookie.getValue();
                    Long usuarioId = jwtUtil.obtenerIdDesdeToken(token);
                    return usuarioService.obtenerUsuarioPorId(usuarioId)
                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                }
            }
        }

        throw new RuntimeException("Token no válido");
    }

}
