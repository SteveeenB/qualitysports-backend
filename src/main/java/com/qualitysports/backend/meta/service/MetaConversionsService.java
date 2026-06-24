package com.qualitysports.backend.meta.service;

import com.qualitysports.backend.meta.config.MetaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetaConversionsService {

    private final MetaProperties props;
    private final RestTemplate restTemplate;

    /**
     * Envía el evento Purchase a Meta Conversions API de forma asíncrona.
     * Nunca bloquea el hilo principal de crearPedido().
     *
     * @param eventId    UUID generado en PedidoService — debe coincidir con el pixel para deduplicación
     * @param pedidoId   ID interno del pedido
     * @param totalNeto  Monto final pagado
     * @param contents   Lista de productos [{id, quantity, item_price}] para Dynamic Ads
     * @param contentIds Lista de IDs de productos (coincide con el catálogo)
     * @param email      Email del comprador (se hashea SHA-256)
     * @param telefono   Teléfono del comprador (se hashea SHA-256)
     * @param nombre     Nombre del comprador (se hashea SHA-256)
     * @param apellido   Apellido del comprador (se hashea SHA-256)
     * @param fbp        Cookie _fbp del navegador (no se hashea)
     * @param fbc        Cookie _fbc del navegador (no se hashea)
     * @param clientIp   IP real del comprador (no se hashea)
     * @param userAgent  User-Agent del navegador del comprador (no se hashea)
     */
    @Async("metaCapiExecutor")
    public void enviarPurchase(
            String eventId, Long pedidoId, BigDecimal totalNeto,
            List<Map<String, Object>> contents, List<String> contentIds,
            String email, String telefono, String nombre, String apellido,
            String municipio, String departamento,
            String fbp, String fbc, String clientIp, String userAgent) {

        if (!props.isEnabled() || props.getPixelId().isBlank()) {
            log.debug("[MetaCAPI] Integración deshabilitada o sin Pixel ID configurado — pedido {}", pedidoId);
            return;
        }

        try {
            // ── user_data: datos hasheados + señales de navegador ──────────────
            Map<String, Object> userData = new HashMap<>();
            putHashedIfPresent(userData, "em", normalizeEmail(email));
            putHashedIfPresent(userData, "ph", normalizePhone(telefono));
            putHashedIfPresent(userData, "fn", normalizeName(nombre));
            putHashedIfPresent(userData, "ln", normalizeName(apellido));

            // Ciudad y departamento se hashean (Meta los requiere así)
            putHashedIfPresent(userData, "ct", normalizeName(municipio));
            putHashedIfPresent(userData, "st", normalizeName(departamento));

            // IP y User-Agent NO se hashean — van en texto plano
            if (clientIp   != null && !clientIp.isBlank())   userData.put("client_ip_address", clientIp);
            if (userAgent  != null && !userAgent.isBlank())   userData.put("client_user_agent", userAgent);
            if (fbp        != null && !fbp.isBlank())         userData.put("fbp", fbp);
            if (fbc        != null && !fbc.isBlank())         userData.put("fbc", fbc);

            // ── custom_data: datos de la compra + productos ────────────────────
            int numItems = contents.stream()
                    .mapToInt(c -> c.get("quantity") instanceof Integer q ? q : 0)
                    .sum();

            Map<String, Object> customData = new HashMap<>();
            customData.put("currency",     "COP");
            customData.put("value",        totalNeto.doubleValue());
            customData.put("order_id",     String.valueOf(pedidoId));
            customData.put("content_type", "product");
            customData.put("contents",     contents);
            customData.put("content_ids",  contentIds);
            customData.put("num_items",    numItems);

            // ── evento ────────────────────────────────────────────────────────
            Map<String, Object> event = new HashMap<>();
            event.put("event_name",       "Purchase");
            event.put("event_time",       Instant.now().getEpochSecond());
            event.put("event_id",         eventId);
            event.put("action_source",    "website");
            event.put("event_source_url", "https://qualitysports-iw7tj.ondigitalocean.app/confirmacion/" + pedidoId);
            event.put("user_data",        userData);
            event.put("custom_data",      customData);

            // ── payload: access_token en el body (no en la URL) ───────────────
            Map<String, Object> payload = new HashMap<>();
            payload.put("data",         List.of(event));
            payload.put("access_token", props.getAccessToken());
            if (!props.getTestEventCode().isBlank()) {
                payload.put("test_event_code", props.getTestEventCode());
            }

            String url = props.getBaseUrl() + "/" + props.getPixelId() + "/events";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> resp = restTemplate.exchange(
                    url, HttpMethod.POST,
                    new HttpEntity<>(payload, headers),
                    String.class);

            log.info("[MetaCAPI] Purchase enviado — pedido={} status={} info={}",
                    pedidoId, resp.getStatusCode().value(), extractSafeInfo(resp.getBody()));

        } catch (Exception e) {
            // Fire-and-forget: nunca propagar excepción — no debe afectar al pedido
            log.error("[MetaCAPI] Error al enviar Purchase pedido={}: {}", pedidoId, e.getMessage());
        }
    }

    // ── Helpers de normalización y hash ──────────────────────────────────────

    private static void putHashedIfPresent(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            String hashed = hashSha256(value);
            if (!hashed.isBlank()) map.put(key, hashed);
        }
    }

    private static String hashSha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static String normalizeEmail(String email) {
        if (email == null) return null;
        return email.toLowerCase(Locale.ROOT).trim();
    }

    private static String normalizePhone(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("\\D", "");
        // Números colombianos móviles: 10 dígitos que empiezan con 3 → agregar código país 57
        if (digits.length() == 10 && digits.startsWith("3")) return "57" + digits;
        return digits;
    }

    private static String normalizeName(String name) {
        if (name == null) return null;
        String lower = name.toLowerCase(Locale.ROOT).trim();
        // Meta exige remover diacríticos: José → jose, Ñoño → nono
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
                         .replaceAll("\\p{M}", "");
    }

    private static String extractSafeInfo(String body) {
        if (body == null) return "null";
        // Solo mostrar los primeros 200 chars — nunca logear el body completo
        // (Meta puede eco el access_token en errores 4xx)
        int limit = Math.min(body.length(), 200);
        return body.substring(0, limit).replaceAll("\"access_token\"\\s*:\\s*\"[^\"]+\"", "\"access_token\":\"[REDACTED]\"");
    }
}
