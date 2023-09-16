package com.mdc.mspring.context.factory.support.impl;

import com.mdc.mspring.context.factory.support.PropertyRegistry;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/15 17:03
 */
public class DefaultPropertyRegistry implements PropertyRegistry {

    private Map<String, String> propertyMap = new ConcurrentHashMap<>();

    @Override
    public void set(String key, String value) {
        if (!propertyMap.containsKey(key)) {
            propertyMap.put(key, value);
        }
    }

    public String getProperty(String name) {
        String defaultVal = null, key = name;
        if (name.startsWith("${") && name.endsWith("}")) {
            int keyEnd = name.indexOf(":");
            if (keyEnd != -1) {
                // parse ${key:defaulVal} expr
                defaultVal = parseProperty(name.substring(name.indexOf(":") + 1, name.length() - 1));
            } else {
                keyEnd = name.length() - 1;
            }
            key = name.substring(2, keyEnd);
        }
        return propertyMap.getOrDefault(key, defaultVal);
    }

    public String parseProperty(String value) {
        if (value.startsWith("${")) {
            return getProperty(value);
        }
        return value;
    }

    @Override
    public void setProperties(Properties properties) {
        properties.forEach((k, v) -> {
            propertyMap.put((String) k, (String) v);
        });
    }
}
