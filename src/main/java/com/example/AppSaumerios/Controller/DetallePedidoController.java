package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.Service.DetallePedidoService;
import com.example.AppSaumerios.Service.PedidoService;
import com.example.AppSaumerios.dto.DetallePedidoDTO;
import com.example.AppSaumerios.dto.DetallePedidoResponseDTO;
import com.example.AppSaumerios.entity.DetallePedido;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/detallePedidos")
@CrossOrigin(origins = "https://front-sahumerios-2.vercel.app")
public class DetallePedidoController {


    @Value("${frontend.url.${spring.profiles.active}}")
    private String frontendUrl;
    @Autowired
    private DetallePedidoService detallePedidoService;

    @Autowired
    private PedidoService pedidoService;

    // =========================
    // Obtener detalle específico de un pedido (usuario)
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerDetallePorId(@PathVariable Long id, HttpServletRequest request) {
        try {
            DetallePedido detalle = detallePedidoService.obtenerDetallePorId(id);
            return ResponseEntity.ok(pedidoService.convertirADTO(detalle));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // =========================
    // Listar todos los detalles de un pedido (usuario)
    // =========================
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<?> obtenerDetallesPorPedido(@PathVariable Long pedidoId) {
        try {
            List<DetallePedido> detalles = detallePedidoService.obtenerDetallesPorPedidoId(pedidoId);
            List<DetallePedidoResponseDTO> detallesDTO = detalles.stream()
                    .map(det -> pedidoService.convertirADTO(det))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(detallesDTO);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}
