package com.mdc.mspring.context.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class YamlUtils {
    public static Map<String, Object> parseYaml(InputStream is) {
        if (is == null) {
            return new HashMap<>();
        }
        return new Yaml().load(is);
    }

    public static Map<String, Object> parseYaml(File f) throws FileNotFoundException {
        return parseYaml(new FileInputStream(f));
    }

    public static Map<String, String> flattenYaml(Map<String, Object> yamlMap) {
        if (yamlMap == null) {
            return Map.of();
        }
        Map<String, String> result = new HashMap<>();
        if (yamlMap == null) {
            return result;
        }
        for (String key : yamlMap.keySet()) {
            Object value = yamlMap.get(key);
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> subMap = flattenYaml((Map<String, Object>) value);
                for (String subKey : subMap.keySet()) {
                    result.put(key + "." + subKey, subMap.get(subKey));
                }
            } else {
                result.put(key, value.toString());
            }
        }
        return result;
    }

}
