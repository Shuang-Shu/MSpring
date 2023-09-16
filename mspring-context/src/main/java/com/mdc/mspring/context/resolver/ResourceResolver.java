package com.mdc.mspring.context.resolver;

import com.mdc.mspring.context.annotation.ComponentScan;
import com.mdc.mspring.context.annotation.Configuration;
import com.mdc.mspring.context.annotation.Import;
import com.mdc.mspring.context.entity.Resource;
import com.mdc.mspring.context.utils.StringUtils;
import com.mdc.mspring.context.utils.YamlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.*;
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
    private final static Logger logger = LoggerFactory.getLogger(ResourceResolver.class);
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

    public ResourceResolver() throws IOException, URISyntaxException, ClassNotFoundException {
        initPropertyMap();
    }

    private void initPropertyMap() throws IOException, URISyntaxException, ClassNotFoundException {
        List<String> configPaths = scanConfigs();
        for (String cfg : configPaths) {
            File file = new File(cfg);
            if (cfg.endsWith(".properties")) {
                parseProperties(file);
            } else if (cfg.endsWith(".yaml") || cfg.endsWith(".yml")) {
                propertyMap.putAll(YamlUtils.flattenYaml(YamlUtils.parseYaml(file)));
            }
        }

    }

    private List<String> scanConfigs() throws IOException, URISyntaxException, ClassNotFoundException {
        List<Resource> resourceCollector = new ArrayList<>();
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("");
        Set<String> suffixSet = Set.of(".yaml", ".yml");
        while (resources.hasMoreElements()) {
            URL resouce = resources.nextElement();
            URI uri = resouce.toURI();
            if (uri.toString().startsWith("file:")) {
                resourceCollector.addAll(getResourcesFromFile(new File(uri), suffixSet, ""));
            }
        }
        return resourceCollector.stream().map(Resource::path).map(p -> p.substring("file:".length())).
                collect(Collectors.toList());
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

    public void scanClassNameOnClass(Class<?> clazz, List<String> classNames, Set<Class<?>> passedClass) throws IOException, URISyntaxException, ClassNotFoundException {
        if (clazz == null || passedClass.contains(clazz) || clazz.isAnnotation()) {
            return;
        } else {
            passedClass.add(clazz);
        }
        if (clazz.getAnnotation(Configuration.class) != null) {
            logger.info("Loading config class {}", clazz.getName());
            // config class
            if (clazz.getAnnotation(ComponentScan.class) != null) {
                String basePackage = null;
                if (StringUtils.isEmpty(basePackage = clazz.getAnnotation(ComponentScan.class).value())) {
                    basePackage = clazz.getPackageName();
                }
                List<Resource> resources = scan(basePackage, t -> t, Set.of(".class"));
                logger.info("Scanning resources: {} in basePackage: {}", resources.stream().map(Resource::name).collect(Collectors.toList()), basePackage);
                for (Resource resource : resources) {
                    scanClassNameOnClass(resource.clazz(), classNames, passedClass);
                }
            }
            if (clazz.getAnnotation(Import.class) != null) {
                logger.info("Importing classes: {}", clazz.getAnnotation(Import.class).value());
                // load all imported classes
                for (Class<?> subClass : clazz.getAnnotation(Import.class).value()) {
                    scanClassNameOnClass(subClass, classNames, passedClass);
                }
            }
        }
        if (!clazz.isAnnotation()) {
            classNames.add(clazz.getName());
        }
    }

    /**
     * @Description: Do scanning in basePackage, all scanned files will be
     * repersented as Resource objects. The mapper will process them.
     * @Param:
     * @Return:
     * @Author: ShuangShu
     * @Date:
     */
    public <R> List<R> scan(String basePackage, Function<Resource, R> mapper, Set<String> suffixSet)
            throws IOException, URISyntaxException, ClassNotFoundException {
        String packagePath = basePackage.replace(".", "/");
        List<Resource> resourceCollector = new ArrayList<>();
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(packagePath);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            URI uri = resource.toURI();
            logger.info("Scanning resource: {}", uri.toString());
            if (uri.toString().startsWith("file:")) {
                resourceCollector.addAll(getResourcesFromFile(new File(uri), suffixSet, basePackage));
            } else if (uri.toString().startsWith("jar:")) {
                JarFile jarFile = ((JarURLConnection) resource.openConnection()).getJarFile();
                resourceCollector.addAll(getResourcesFromJar(jarFile, uri, packagePath, suffixSet));
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
    private List<Resource> getResourcesFromFile(File file, final Set<String> suffixSet, String basePackage) throws IOException, ClassNotFoundException {
        List<Resource> result = new ArrayList<>();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            assert files != null;
            for (File f : files) {
                result.addAll(getResourcesFromFile(f, suffixSet, basePackage));
            }
        } else {
            String suffix = file.getPath().substring(file.getPath().lastIndexOf("."));
            if (suffixSet.contains(suffix)) {
                if (".class".equals(suffix)) {
                    String fullName = file.getPath().substring(file.getPath().indexOf(basePackage.replace(".", "/")));
                    String formatFullName = fullName.replace("/", ".");
                    formatFullName = formatFullName.substring(0, formatFullName.lastIndexOf("."));
                    result.add(new Resource("file:" + file.getPath(), fullName, Class.forName(formatFullName)));
                    ;
                } else {
                    result.add(new Resource("file:" + file.getPath(), "", null));
                }
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
    private List<Resource> getResourcesFromJar(JarFile jar, URI uri, String basePackage, final Set<String> suffixSet) throws ClassNotFoundException {
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
            if (name.endsWith(".class")) {
                String formatFullName = name.replace("/", ".");
                formatFullName = formatFullName.substring(0, formatFullName.lastIndexOf("."));
                var targetClass = Class.forName(formatFullName);
                result.add(new Resource(uriStr.substring(0, uriStr.length() - basePackage.length() - 1), name, targetClass));
            } else {
                result.add(new Resource(uriStr.substring(0, uriStr.length() - basePackage.length() - 1), name, null));
            }
        }
        return result;
    }
}