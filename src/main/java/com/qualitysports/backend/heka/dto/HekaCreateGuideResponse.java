package com.qualitysports.backend.heka.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HekaCreateGuideResponse(
        int code,
        GuideData response
) {
    // Clase (no record) para poder usar @JsonAnySetter y capturar cualquier clave
    // que Heka devuelva, sin importar si viene en camelCase o snake_case
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static final class GuideData {

        private final Map<String, Object> fields = new LinkedHashMap<>();

        @JsonAnySetter
        public void setField(String key, Object value) {
            fields.put(key, value);
        }

        public String guideNumber() {
            return get("guide_number", "guideNumber", "guide", "numero_guia", "numeroGuia", "number");
        }

        public String shipmentId() {
            return get("shipment_id", "shipmentId", "_id", "id", "shipId");
        }

        public String status() {
            return get("status", "statusCode", "state", "estado");
        }

        // Para logging — muestra todos los campos recibidos de Heka
        public String allFields() {
            return fields.toString();
        }

        private String get(String... keys) {
            for (String key : keys) {
                Object val = fields.get(key);
                if (val != null) return String.valueOf(val);
            }
            return null;
        }
    }
}
