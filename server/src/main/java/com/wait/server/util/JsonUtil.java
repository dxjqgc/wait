package com.wait.server.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("serialize json failed", e);
        }
    }

    public static List<String> toStringList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    public static List<Map<String, String>> toMapList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return MAPPER.readValue(json, new TypeReference<List<Map<String, String>>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    public static Map<String, Object> toMap(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }
}
