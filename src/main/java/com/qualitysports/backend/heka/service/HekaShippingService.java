package com.qualitysports.backend.heka.service;

import com.qualitysports.backend.heka.config.HekaProperties;
import com.qualitysports.backend.heka.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HekaShippingService {

    private final HekaProperties props;
    private final HekaTokenService tokenService;
    private final RestTemplate restTemplate;

    public HekaCityResponse searchCity(String label) {
        String url = UriComponentsBuilder
                .fromUriString(props.getBaseUrl() + "/api/v1/geolocation/city")
                .queryParam("label", label)
                .toUriString();

        try {
            HttpHeaders headers = apiKeyHeaders();
            ResponseEntity<HekaCityResponse> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), HekaCityResponse.class);
            return resp.getBody();
        } catch (HttpClientErrorException e) {
            log.error("[Heka] searchCity HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error al consultar ciudades en HekaEntrega: " + e.getStatusCode());
        } catch (RestClientException e) {
            log.error("[Heka] searchCity connection error: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "HekaEntrega no disponible");
        }
    }

    public HekaQuoteResponse quote(HekaQuoteRequest req) {
        if (props.getApiKey() == null || props.getApiKey().isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Integración HekaEntrega no configurada. Configura las variables de entorno HEKA_*.");
        }
        Map<String, Object> body = new HashMap<>();
        body.put("city_origin", props.getCityOrigin());
        body.put("city_destination", req.cityDestination());
        body.put("type_payment", req.typePayment());
        body.put("declared_value", req.declaredValue());
        body.put("weight", req.weight());
        body.put("height", req.height());
        body.put("long", req.longDim());
        body.put("width", req.width());
        body.put("withshipping_cost", req.withshippingCost());
        body.put("collection_value", req.collectionValue());

        String url = props.getBaseUrl() + "/api/v1/shipping/quoter?quoter=1";
        try {
            ResponseEntity<HekaQuoteResponse> resp = restTemplate.exchange(
                    url, HttpMethod.POST,
                    new HttpEntity<>(body, bearerHeaders()),
                    HekaQuoteResponse.class);
            return resp.getBody();
        } catch (HttpClientErrorException e) {
            log.error("[Heka] quote HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error al cotizar en HekaEntrega: " + e.getStatusCode());
        } catch (RestClientException e) {
            log.error("[Heka] quote connection error: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "HekaEntrega no disponible");
        }
    }

    public HekaCreateGuideResponse createGuide(HekaCreateGuideRequest req,
            String clientName, String clientLastName,
            String address, String phone,
            String neighborhood, String document) {
        Map<String, Object> clientData = new HashMap<>();
        clientData.put("name", clientName);
        clientData.put("last_name", clientLastName);
        clientData.put("address", address);
        clientData.put("phone", Long.parseLong(phone.replaceAll("\\D", "")));
        clientData.put("type_document", "CC");
        clientData.put("document", document != null && !document.isBlank() ? document : "0");
        clientData.put("neighborhood", neighborhood != null ? neighborhood : "");
        clientData.put("note_destination", "");

        Map<String, Object> body = new HashMap<>();
        body.put("type", 1);
        body.put("city_origin", props.getCityOrigin());
        body.put("city_destination", req.cityDestination());
        body.put("type_payment", 1);
        body.put("total", req.total());
        body.put("declared_value", req.declaredValue());
        body.put("weight", req.weight());
        body.put("height", req.height());
        body.put("long", req.longDim());
        body.put("width", req.width());
        body.put("withshipping_cost", false);
        body.put("collection_value", req.collectionValue());
        if (req.distributorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "distributorId es requerido");
        }
        body.put("distributor_id", req.distributorId());
        body.put("seller", "");
        body.put("quantity", 1);
        body.put("note", req.note() != null ? req.note() : "");
        body.put("product", req.product());
        body.put("extra_info", "");
        body.put("warehouse", props.getWarehouseId());
        body.put("client", clientData);
        body.put("in_order_form", false);
        body.put("collection_request", false);
        body.put("inventory_allowed", false);

        String url = props.getBaseUrl() + "/api/v1/shipments/guide";
        try {
            ResponseEntity<HekaCreateGuideResponse> resp = restTemplate.exchange(
                    url, HttpMethod.POST,
                    new HttpEntity<>(body, bearerHeaders()),
                    HekaCreateGuideResponse.class);
            HekaCreateGuideResponse body2 = resp.getBody();
            if (body2 != null && body2.response() != null) {
                log.info("[Heka] createGuide OK — guideNumber={} shipmentId={} status={}",
                        body2.response().guideNumber(),
                        body2.response().shipmentId(),
                        body2.response().status());
            } else {
                log.warn("[Heka] createGuide respuesta vacía o sin response: {}", body2);
            }
            return body2;
        } catch (HttpClientErrorException e) {
            log.error("[Heka] createGuide HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error al generar guía en HekaEntrega: " + e.getStatusCode());
        } catch (RestClientException e) {
            log.error("[Heka] createGuide connection error: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "HekaEntrega no disponible");
        }
    }

    public void logWarehouses() {
        String url = props.getBaseUrl() + "/api/v1/warehouse";
        try {
            HttpHeaders h = new HttpHeaders();
            h.setBearerAuth(tokenService.getToken());
            ResponseEntity<String> resp = new RestTemplate().exchange(
                    url, HttpMethod.GET, new HttpEntity<>(h), String.class);
            log.info("[Heka] BODEGAS DISPONIBLES:\n{}", resp.getBody());
        } catch (Exception e) {
            log.error("[Heka] No se pudieron obtener bodegas: {}", e.getMessage());
        }
    }

    public HekaProperties getDefaults() {
        return props;
    }

    private HttpHeaders bearerHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setBearerAuth(tokenService.getToken());
        return h;
    }

    private HttpHeaders apiKeyHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.set("Api-Key", props.getApiKey());
        return h;
    }
}
