package com.qualitysports.backend.heka.service;

import com.qualitysports.backend.heka.config.HekaProperties;
import com.qualitysports.backend.heka.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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

        HttpHeaders headers = apiKeyHeaders();
        ResponseEntity<HekaCityResponse> resp = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), HekaCityResponse.class);
        return resp.getBody();
    }

    public HekaQuoteResponse quote(HekaQuoteRequest req) {
        Map<String, Object> body = new HashMap<>();
        body.put("city_origin",       props.getCityOrigin());
        body.put("city_destination",  req.cityDestination());
        body.put("type_payment",      req.typePayment());
        body.put("declared_value",    req.declaredValue());
        body.put("weight",            req.weight());
        body.put("height",            req.height());
        body.put("long",              req.longDim());
        body.put("width",             req.width());
        body.put("withshipping_cost", req.withshippingCost());
        body.put("collection_value",  req.collectionValue());

        String url = props.getBaseUrl() + "/api/v1/shipping/quoter?quoter=1";
        ResponseEntity<HekaQuoteResponse> resp = restTemplate.exchange(
                url, HttpMethod.POST,
                new HttpEntity<>(body, bearerHeaders()),
                HekaQuoteResponse.class);
        return resp.getBody();
    }

    public HekaCreateGuideResponse createGuide(HekaCreateGuideRequest req,
                                               String clientName, String clientLastName,
                                               String address, String phone,
                                               String neighborhood) {
        Map<String, Object> clientData = new HashMap<>();
        clientData.put("name",              clientName);
        clientData.put("last_name",         clientLastName);
        clientData.put("address",           address);
        clientData.put("phone",             Long.parseLong(phone.replaceAll("\\D", "")));
        clientData.put("type_document",     "CC");
        clientData.put("document",          "0");
        clientData.put("neighborhood",      neighborhood != null ? neighborhood : "");
        clientData.put("note_destination",  "");

        Map<String, Object> body = new HashMap<>();
        body.put("type",              1);
        body.put("city_origin",       props.getCityOrigin());
        body.put("city_destination",  req.cityDestination());
        body.put("type_payment",      1);
        body.put("total",             req.total());
        body.put("declared_value",    req.declaredValue());
        body.put("weight",            req.weight());
        body.put("height",            req.height());
        body.put("long",              req.longDim());
        body.put("width",             req.width());
        body.put("withshipping_cost", false);
        body.put("collection_value",  req.collectionValue());
        body.put("distributor_id",    req.distributorId());
        body.put("seller",            "");
        body.put("quantity",          1);
        body.put("note",              req.note() != null ? req.note() : "");
        body.put("product",           req.product());
        body.put("extra_info",        "");
        body.put("warehouse",         props.getWarehouseId());
        body.put("client",            clientData);
        body.put("in_order_form",     false);
        body.put("collection_request", false);
        body.put("inventory_allowed", false);

        String url = props.getBaseUrl() + "/api/v1/shipments/guide";
        ResponseEntity<HekaCreateGuideResponse> resp = restTemplate.exchange(
                url, HttpMethod.POST,
                new HttpEntity<>(body, bearerHeaders()),
                HekaCreateGuideResponse.class);
        return resp.getBody();
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
