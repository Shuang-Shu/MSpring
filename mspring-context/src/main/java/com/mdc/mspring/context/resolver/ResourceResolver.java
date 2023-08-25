package com.mdc.mspring.context.resolver;

import com.mdc.mspring.context.entity.Resource;

import com.mdc.mspring.context.utils.YamlUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Description: ResourceResolver loads
 */
public class ResourceResolver {
    private final String basePackage; // format: com.example.demo
    public static final Set<String> CLASS_SUFFIX = Set.of(".class");

    // for config scanning
    private final Map<String, String> propertyMap = new HashMap<>();
    private final Set<String> CONFIG_SET = Set.of(new String[]{".yaml"});
    private final static Map<Class<?>, Function<String, ?>> pasers = new HashMap<>();

    static {
        // String类型:
        pasers.put(String.class, s -> s);
        // boolean类型:
        pasers.put(boolean.class, Boolean::parseBoolean);
        pasers.put(Boolean.class, Boolean::valueOf);
        // int类型:
        pasers.put(int.class, Integer::parseInt);
        pasers.put(Integer.class, Integer::valueOf);
        // 其他基本类型...
        // Date/Time类型:
        pasers.put(LocalDate.class, LocalDate::parse);
        pasers.put(LocalTime.class, LocalTime::parse);
        pasers.put(LocalDateTime.class, LocalDateTime::parse);
        pasers.put(ZonedDateTime.class, ZonedDateTime::parse);
        pasers.put(Duration.class, Duration::parse);
        pasers.put(ZoneId.class, ZoneId::of);
    }

    public ResourceResolver(String basePackage) throws IOException, URISyntaxException {
        this.basePackage = basePackage;
        initMap();
    }

    public void setProperties(Properties properties) {
        for (String name : properties.stringPropertyNames()) {
            properties.put(name, properties.getProperty(name));
        }
    }

    private void initMap() throws IOException, URISyntaxException {
        List<String> configPaths = scan("", Resource::name, CONFIG_SET, 2);
        for (String cfg : configPaths) {
            File file = new File(cfg);
            if (cfg.endsWith(".properties")) {
                parseProperties(file);
            } else if (cfg.endsWith(".yaml")) {
                YamlUtils.parseYaml(file);
            }
        }
    }

    private void parseProperties(File f) throws IOException {
        try (BufferedReader bfr = new BufferedReader(new InputStreamReader(new FileInputStream(f)));) {
            String line;
            while ((line = bfr.readLine()) != null) {
                int index = line.indexOf("=");
                String key = line.substring(0, index);
                String value = line.substring(index + 1);
                propertyMap.put(key, value);
            }
        }
    }

    public Map<String, String> getCopiedMap() {
        return new HashMap<>(this.propertyMap);
    }

    /**
     * @Description: Do scanning in basePackage, all scanned files will be
     * repersented as Resource objects. The mapper will process them.
     * @Param:
     * @Return:
     * @Author: ShuangShu
     * @Date:
     */
    public <R> List<R> scanClass(Function<Resource, R> mapper) throws IOException, URISyntaxException {
        // get all files in classpath/basePackage
        return scan(this.basePackage, mapper, CLASS_SUFFIX, -1);
    }

    public <R> List<R> scan(String basePackage, Function<Resource, R> mapper, Set<String> suffixSet, int level)
            throws IOException, URISyntaxException {
        String packagePath = basePackage.replace(".", "/");
        List<Resource> resourceCollector = new ArrayList<>();
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(packagePath);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            URI uri = resource.toURI();

            if (uri.toString().startsWith("file:")) {
                resourceCollector.addAll(getResourcesFromFile(new File(uri), suffixSet, level));
            } else if (uri.toString().startsWith("jar:")) {
                if (level < 0) {
                    JarFile jarFile = ((JarURLConnection) resource.openConnection()).getJarFile();
                    resourceCollector.addAll(getResourcesFromJar(jarFile, uri, packagePath, suffixSet));
                }
            }
        }
        return resourceCollector.stream().map(mapper).collect(Collectors.toList());
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
    @SuppressWarnings("unchecked")
    public static <T> T parseTo(Class<?> target, String value) {
        return (T) pasers.get(target).apply(value);
    }

    /**
     * @Description: Get resources from file
     * @Param:
     * @Return:
     * @Author: ShuangShu
     * @Date: 2023/8/10 18:20
     */
    private List<Resource> getResourcesFromFile(File file, final Set<String> suffixSet, int level) throws IOException {
        List<Resource> result = new ArrayList<>();
        if (file.isDirectory()) {
            if (level != 0) {
                File[] files = file.listFiles();
                assert files != null;
                for (File f : files) {
                    result.addAll(getResourcesFromFile(f, suffixSet, level - 1));
                }
            }
        } else {
            String suffix = file.getPath().substring(file.getPath().lastIndexOf("."));
            if (suffixSet.contains(suffix)) {
                String fullName = file.getPath().substring(file.getPath().indexOf(basePackage.replace(".", "/")));
                result.add(new Resource("file:" + file.getPath(), fullName));
            }
        }
        return result;
    }

    /**
     * @Description: Get resources from JarFile
     * @Param:
     * @Return:
     * @Author: ShuangShu
     * @Date: 2023/8/10 18:22
     */
    private List<Resource> getResourcesFromJar(JarFile jar, URI uri, String basePackage, final Set<String> suffixSet) {
        List<Resource> result = new ArrayList<>();
        Enumeration<JarEntry> entries = jar.entries();
        JarEntry jarEntry;
        String name;
        while (entries.hasMoreElements()) {
            jarEntry = entries.nextElement();
            name = jarEntry.getName();
            if (name.startsWith("/")) {
                name = name.substring(1);
            }
            if (jarEntry.isDirectory() || !name.startsWith(basePackage)) {
                continue;
            }
            String suffix = name.substring(name.lastIndexOf("."));
            if (!suffixSet.contains(suffix)) {
                continue;
            }
            String uriStr = uri.toString();
            result.add(new Resource(uriStr.substring(0, uriStr.length() - basePackage.length() - 1), name));
        }
        return result;
    }
}