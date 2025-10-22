package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.entity.EstadoPedido;
import com.example.AppSaumerios.entity.Pedidos;
import com.example.AppSaumerios.entity.LogNotificacionPago;
import com.example.AppSaumerios.repository.LogNotificacionPagoRepository;
import com.example.AppSaumerios.repository.PedidoRepository;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoService {

    @Value("${MERCADOPAGO_ACCESS_TOKEN}")
    private String accessToken;

    @Value("${APP_FRONTEND_URL}")
    private String frontendUrl;

    private final PedidoRepository pedidoRepository;
    private final LogNotificacionPagoRepository logNotificacionRepository;

    public MercadoPagoService(PedidoRepository pedidoRepository,
                              LogNotificacionPagoRepository logNotificacionRepository) {
        this.pedidoRepository = pedidoRepository;
        this.logNotificacionRepository = logNotificacionRepository;
    }

    public String crearPreferenciaPago(Pedidos pedido) {
        try {
            // ✅ Configurar el token de Mercado Pago
            MercadoPagoConfig.setAccessToken(accessToken);

            // ✅ Armar los ítems desde el pedido
            List<PreferenceItemRequest> items = pedido.getDetalles().stream()
                    .map(detalle -> PreferenceItemRequest.builder()
                            .id(detalle.getProducto().getId().toString())
                            .title(detalle.getProducto().getNombre())
                            .description(detalle.getProducto().getDescripcion())
                            .categoryId("general")
                            .quantity(detalle.getCantidad())
                            .currencyId("ARS")
                            .unitPrice(detalle.getProducto().getPrecio()) // ya es BigDecimal
                            .build())
                    .toList();

            if (items.isEmpty()) {
                throw new IllegalArgumentException("El pedido no contiene productos válidos para generar la preferencia.");
            }

            // ✅ URLs del backend (ajustá el dominio o túnel local)
            String baseUrl = "https://miapptest.loca.lt"; // ⚠️ Cambiar si usás ngrok o dominio real
            String successUrl = baseUrl + "/api/pagos/exito?pedido_id=" + pedido.getId();
            String failureUrl = baseUrl + "/api/pagos/fallo?pedido_id=" + pedido.getId();
            String pendingUrl = baseUrl + "/api/pagos/pendiente?pedido_id=" + pedido.getId();

            // ✅ Configurar las URLs de retorno
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl)
                    .failure(failureUrl)
                    .pending(pendingUrl)
                    .build();

            // ✅ Crear la preferencia
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .externalReference(pedido.getId().toString())
                    .statementDescriptor("AppSaumerios")
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // ✅ Guardar el ID de preferencia en el pedido
            pedido.setPreferenciaId(preference.getId());
            pedidoRepository.save(pedido);

            // ✅ Retornar el link de pago para redirigir al usuario
            return preference.getInitPoint();

        } catch (MPApiException ex) {
            System.err.println("⚠️ Error de API de MercadoPago: " + ex.getApiResponse().getContent());
            throw new RuntimeException("Error al crear preferencia de pago: " + ex.getMessage());
        } catch (MPException ex) {
            System.err.println("⚠️ Error general de MercadoPago: " + ex.getMessage());
            throw new RuntimeException("Error al crear preferencia de pago: " + ex.getMessage());
        }
    }

    public void procesarNotificacion(String id, String topic, String rawData) {
        try {
            // Registrar notificación
            LogNotificacionPago log = new LogNotificacionPago();
            log.setTipoNotificacion(topic);
            log.setIdNotificacion(id);
            log.setDatosCompletos(rawData);
            logNotificacionRepository.save(log);

            if ("payment".equals(topic)) {
                MercadoPagoConfig.setAccessToken(accessToken);
                PaymentClient client = new PaymentClient();
                Payment payment = client.get(Long.parseLong(id)); // ✅ ahora es Payment

                String externalReference = payment.getExternalReference();
                if (externalReference != null) {
                    Long pedidoId = Long.parseLong(externalReference);
                    Pedidos pedido = pedidoRepository.findById(pedidoId)
                            .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));

                    pedido.setPagoId(payment.getId().toString());
                    pedido.setEstadoPago(payment.getStatus());
                    pedido.setMetodoPago(payment.getPaymentTypeId());
                    pedido.setFechaActualizacionPago(LocalDateTime.now());

                    if ("approved".equals(payment.getStatus())) {
                        pedido.setEstado(EstadoPedido.PAGADO);
                        pedido.setFechaAprobacionPago(LocalDateTime.now());
                    } else if ("rejected".equals(payment.getStatus())) {
                        pedido.setEstado(EstadoPedido.CANCELADO);
                    }

                    pedidoRepository.save(pedido);

                    log.setPedido(pedido);
                    log.setIdPago(payment.getId().toString());
                    log.setIdPreferencia(pedido.getPreferenciaId());
                    log.setEstadoPago(payment.getStatus());
                    logNotificacionRepository.save(log);
                }
            }
        } catch (Exception e) {
            System.err.println("Error procesando notificación: " + e.getMessage());
        }
    }

    public Payment obtenerPago(String pagoId) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            return client.get(Long.parseLong(pagoId)); // ✅ devuelve Payment
        } catch (MPApiException | MPException e) {
            System.err.println("Error obteniendo pago: " + e.getMessage());
            return null;
        }
    }

    public void actualizarEstadoPago(String pagoId) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(Long.parseLong(pagoId)); // ✅ ahora Payment

            Pedidos pedido = pedidoRepository.findByPagoId(pagoId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado para pago: " + pagoId));

            pedido.setEstadoPago(payment.getStatus());
            pedido.setFechaActualizacionPago(LocalDateTime.now());

            if ("approved".equals(payment.getStatus())) {
                pedido.setEstado(EstadoPedido.PAGADO);
            } else if ("rejected".equals(payment.getStatus())) {
                pedido.setEstado(EstadoPedido.CANCELADO);
            }

            pedidoRepository.save(pedido);

        } catch (MPApiException | MPException e) {
            System.err.println("Error al actualizar estado de pago: " + e.getMessage());
        }
    }
}
