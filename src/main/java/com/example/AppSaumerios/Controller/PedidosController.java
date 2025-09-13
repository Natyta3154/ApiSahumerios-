package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.dto.*;
import com.example.AppSaumerios.entity.Pedidos;
import com.example.AppSaumerios.Service.PedidoService;
import com.example.AppSaumerios.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/pedidos")
@CrossOrigin(origins = "http://localhost:9002")
public class PedidosController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private JwtUtil jwtUtil;

    // =========================
    // Crear nuevo pedido
    // =========================
    @PostMapping("/realizarPedido")
    public ResponseEntity<?> crearPedido(@RequestBody CrearPedidoRequestDTO crearPedidoRequestDTO) {
        try {
            Pedidos pedido = pedidoService.crearPedido(crearPedidoRequestDTO);
            List<DetallePedidoDTO> detallesDTO = pedido.getDetalles().stream()
                    .map(det -> pedidoService.convertirADTO(det))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(detallesDTO);
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

            List<Object> pedidosDTO = pedidos.stream().map(p -> {
                return new Object() {
                    public final Long id = p.getId();
                    public final String estado = p.getEstado().name();
                    public final List<DetallePedidoDTO> detalles = p.getDetalles().stream()
                            .map(det -> pedidoService.convertirADTO(det))
                            .collect(Collectors.toList());
                    public final Object total = p.getTotal();
                    public final Object fecha = p.getFecha();
                };
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
    // Helper para obtener usuario desde JWT
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
