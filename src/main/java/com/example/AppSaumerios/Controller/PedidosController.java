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
public class PedidosController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private JwtUtil jwtUtil;

    // Crear nuevo pedido
    @PostMapping("/relizarPedidos")
    public ResponseEntity<?> crearPedido(@Valid @RequestBody CrearPedidoRequestDTO crearPedidoRequestDTO,
                                         HttpServletRequest httpRequest) {
        try {
            // Verificar que el usuario del token coincide con el del DTO
            Long usuarioIdToken = obtenerUsuarioIdDesdeToken(httpRequest);
            if (!usuarioIdToken.equals(crearPedidoRequestDTO.getUsuarioId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No puedes crear pedidos para otros usuarios");
            }

            Pedidos pedido = pedidoService.crearPedido(crearPedidoRequestDTO);
            PedidoResponseDTO response = convertirAPedidoResponseDTO(pedido);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear pedido: " + e.getMessage());
        }
    }

    // Obtener pedidos del usuario actual
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> obtenerPedidosUsuario(HttpServletRequest httpRequest) {
        Long usuarioId = obtenerUsuarioIdDesdeToken(httpRequest);
        List<Pedidos> pedidos = pedidoService.obtenerPedidosPorUsuario(usuarioId);

        List<PedidoResponseDTO> responses = pedidos.stream()
                .map(this::convertirAPedidoResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // Obtener detalle de un pedido específico
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPedido(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            Long usuarioId = obtenerUsuarioIdDesdeToken(httpRequest);
            Pedidos pedido = pedidoService.obtenerPedidoPorIdYUsuario(id, usuarioId);
            PedidoResponseDTO response = convertirAPedidoResponseDTO(pedido);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pedido no encontrado");
        }
    }

    // Admin: obtener todos los pedidos
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerTodosPedidos() {
        List<Pedidos> pedidos = pedidoService.obtenerTodosPedidos();
        List<PedidoResponseDTO> responses = pedidos.stream()
                .map(this::convertirAPedidoResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // Admin: actualizar estado del pedido
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id,
                                              @RequestParam String estado) {
        try {
            // Validar estado
            if (!List.of("PENDIENTE", "PAGADO", "ENVIADO", "CANCELADO").contains(estado)) {
                return ResponseEntity.badRequest().body("Estado no válido");
            }

            Pedidos pedido = pedidoService.actualizarEstado(id, estado);
            return ResponseEntity.ok("Estado actualizado correctamente a: " + estado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar estado: " + e.getMessage());
        }
    }

    private Long obtenerUsuarioIdDesdeToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.obtenerIdDesdeToken(token);
        }
        throw new RuntimeException("Token no válido");
    }

    private PedidoResponseDTO convertirAPedidoResponseDTO(Pedidos pedido) {
        PedidoResponseDTO response = new PedidoResponseDTO();
        response.setId(pedido.getId());
        response.setFecha(pedido.getFecha());
        response.setTotal(pedido.getTotal());
        response.setEstado(pedido.getEstado().name()); // ✅ .name() convierte Enum a String
        response.setUsuarioId(pedido.getUsuario().getId());
        response.setUsuarioNombre(pedido.getUsuario().getNombre());

        response.setDetalles(pedido.getDetalles().stream()
                .map(this::convertirADetallePedidoResponseDTO) // ✅ Método actualizado
                .collect(Collectors.toList()));

        return response;
    }

    // ✅ NUEVO MÉTODO: Convierte a DetallePedidoResponseDTO con información completa del producto
    private DetallePedidoResponseDTO convertirADetallePedidoResponseDTO(com.example.AppSaumerios.entity.DetallePedido detalle) {
        DetallePedidoResponseDTO dto = new DetallePedidoResponseDTO();
        dto.setId(detalle.getId());
        dto.setPedidoId(detalle.getPedido().getId());
        dto.setProductoId(detalle.getProducto().getId());
        dto.setProductoNombre(detalle.getProducto().getNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getProducto().getPrecio());
        dto.setSubtotal(detalle.getSubtotal());
        dto.setImagenUrl(detalle.getProducto().getImagenurl());
        return dto;
    }
}