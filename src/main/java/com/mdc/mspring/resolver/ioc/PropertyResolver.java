package com.mdc.mspring.resolver.ioc;

import com.mdc.mspring.entity.Resource;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/10/18:31
 * @Description:
 */
public class PropertyResolver {
    private final Map<String, String> propertyMap = new HashMap<>();
    private final ResourceResolver resolver = new ResourceResolver("");
    private final Set<String> CONFIG_SET = Set.of(new String[]{".yaml", ".properties"});

    public PropertyResolver() throws IOException, URISyntaxException {
        initMap();
    }

    private void initMap() throws IOException, URISyntaxException {
        List<String> configPaths = resolver.scan("", Resource::name, CONFIG_SET, 2);
        for (String cfg : configPaths) {
            File file = new File(cfg);
            if (cfg.endsWith(".properties")) {
                parseProperties(file);
            } else if (cfg.endsWith(".yaml")) {
                parseYaml(file);
            }
        }
    }

    private void parseProperties(File f) throws IOException {
        BufferedReader bfr = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        String line;
        while ((line = bfr.readLine()) != null) {
            int index = line.indexOf("=");
            String key = line.substring(0, index);
            String value = line.substring(index + 1);
            propertyMap.put(key, value);
        }
    }

    private void parseYaml(File f) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        Map<String, Object> yamlMap = yaml.load(new FileInputStream(f));
        propertyMap.putAll(flattenYaml(yamlMap));
    }

    private Map<String, String> flattenYaml(Map<String, Object> yamlMap) {
        Map<String, String> result = new HashMap<>();
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

    public String parseValue(String value) {
        if (value.startsWith("${")) {
            return getProperty(value);
        }
        return value;
    }

    public String getProperty(String name) {
        String defaultVal = null, key = name;
        if (name.startsWith("${") && name.endsWith("}")) {
            int keyEnd = name.indexOf(":");
            if (keyEnd != -1) {
                // parse ${key:defaulVal} expr
                defaultVal = parseValue(name.substring(name.indexOf(":") + 1, name.length() - 1));
            } else {
                keyEnd = name.length() - 1;
            }
            key = name.substring(2, keyEnd);
        }
        return propertyMap.getOrDefault(key, defaultVal);
    }

    /**
     * @Description: Parse value to target type T
     * @Param: [mapper, value]
     * @Return: Target type object
     * @Author: ShuangShu
     * @Date: 2023/8/10 21:50
     */
    public static <T> T parseTo(Function<String, T> mapper, String value) {
        return mapper.apply(value);
    }
}
