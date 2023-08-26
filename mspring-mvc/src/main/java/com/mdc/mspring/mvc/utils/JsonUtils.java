package com.mdc.mspring.mvc.utils;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdc.mspring.context.utils.StringUtils;

public class JsonUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T parseObject(String jsonStr, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonStr, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> parseMap(String jsonStr) {
        try {
            return StringUtils.isEmpty(jsonStr) ? new HashMap<>() : objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String writeJson(Object body) throws JsonProcessingException {
        return objectMapper.writeValueAsString(body);
    }

    public static String writeJson(Map<String, Object> map) throws JsonProcessingException {
        return objectMapper.writeValueAsString(map);
    }
}
