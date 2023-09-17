package com.mdc.mspring.context.resolver;

import com.mdc.mspring.context.annotation.*;
import com.mdc.mspring.context.common.ResourceType;
import com.mdc.mspring.context.factory.support.BeanDefinition;
import com.mdc.mspring.context.factory.support.BeanDefinitionRegistry;
import com.mdc.mspring.context.factory.support.PropertyRegistry;
import com.mdc.mspring.context.io.Resource;
import com.mdc.mspring.context.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.time.*;
import java.util.*;
import java.util.function.Function;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/15 14:02
 */
// TODO：可对ResourceResolver进行抽象化
public class ClassPathResourceResolver {
    private final Logger logger = LoggerFactory.getLogger(ClassPathResourceResolver.class);
    private final BeanDefinitionRegistry registry;
    private final PropertyRegistry propertyRegistry;
    private final Set<String> scannedPackages = new HashSet<>();
    private final Set<Class<?>> scannedClasses = new HashSet<>();

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

    public ClassPathResourceResolver(BeanDefinitionRegistry registry, PropertyRegistry propertyRegistry) {
        this.registry = registry;
        this.propertyRegistry = propertyRegistry;
    }

    public void scanOn(Class<?> configClass) throws IOException, URISyntaxException, ClassNotFoundException, NoSuchMethodException {
        if (scannedClasses.contains(configClass)) {
            return;
        } else {
            scannedClasses.add(configClass);
        }
        if (ClassUtils.getAnnotation(configClass, Configuration.class) == null) {
            return;
        }
        logger.info("Scanning on class: {}", configClass.getName());
        String scanPackage = configClass.getPackageName();
        ComponentScan componentScan = null;
        if ((componentScan = ClassUtils.getAnnotation(configClass, ComponentScan.class)) != null) {
            scanPackage = componentScan.value();
        }
        scanOn(scanPackage, configClass);
        defaultConfigParse();
    }

    private void defaultConfigParse() throws IOException, URISyntaxException {
        String[] defaultLocations = {"application.yaml", "application.properties", "application.yml", "config/application.yaml", "config/application.properties", "config/application.yml"};
        for (String location : defaultLocations) {
            parseConfig(location);
        }
    }

    public void scanOn(String scanPackage, Class<?> configClass) throws IOException, URISyntaxException, ClassNotFoundException, NoSuchMethodException {
        // avoid duplicate scanning
        if (scannedPackages.contains(scanPackage)) {
            return;
        } else {
            scannedPackages.add(scanPackage);
        }
        logger.info("Scanning on package: {}", scanPackage);
        scanPackage = scanPackage.replace(".", "/");
        final String scanPackagePath = scanPackage;
        List<Resource> scannedResources = new ArrayList<>(ResourceUtils.getAllResources(scanPackagePath));
        final Set<Class<?>> candidateConfig = new HashSet<>();
        if (configClass != null) {
            candidateConfig.add(configClass);
        }
        scannedResources.stream().filter(r -> r.getType() == ResourceType.CLASS).map(
                        r -> {
                            String className = UrlUtils.convertToClassName(r.getName(), scanPackagePath);
                            Class<?> clazz = null;
                            try {
                                clazz = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                            return clazz;
                        }
                ).filter(rc -> !rc.isAnnotation())
                .filter(rc -> ClassUtils.getAnnotation(rc, Component.class) != null).map(
                        rc -> {
                            try {
                                return buildBeanDefinition(rc);
                            } catch (NoSuchMethodException e) {
                                throw new RuntimeException(e);
                            }
                        }
                ).filter(Objects::nonNull).forEach(bd -> {
                    registry.registerBeanDefinition(bd.getBeanName(), bd);
                    if (ClassUtils.getAnnotation(bd.getDeclaredClass(), Configuration.class) != null) {
                        candidateConfig.add(bd.getDeclaredClass());
                    }
                });
        // 2 deal with configuration bean
        for (Class<?> aClass : candidateConfig) {
            handleCandidateConfigs(aClass);
        }
    }

    private void handleCandidateConfigs(Class<?> clazz) throws IOException, URISyntaxException, ClassNotFoundException, NoSuchMethodException {
        if (ClassUtils.getAnnotation(clazz, Configuration.class) == null) {
            throw new RuntimeException("Class " + clazz.getName() + " is not a configuration class.");
        }
        BeanDefinition configDefinition = buildBeanDefinition(clazz);
        registry.registerBeanDefinition(configDefinition.getBeanName(), configDefinition);
        // 0 load config bean
        for (var method : clazz.getDeclaredMethods()) {
            if (method.getAnnotation(Bean.class) != null) {
                method.setAccessible(true);
                Bean bean = method.getAnnotation(Bean.class);
                String beanName = bean.value();
                if (StringUtils.isEmpty(beanName)) {
                    beanName = StringUtils.getFirstCharLowerCase(method.getReturnType().getSimpleName());
                }
                Method initialMethod = null, destroyMethod = null;
                BeanDefinition beanDefinition = BeanDefinition.builder().beanName(beanName)
                        .declaredClass(method.getReturnType()).initMethod(initialMethod).destroyMethod(destroyMethod)
                        .order(ClassUtils.getOrder(method))
                        .primary(ClassUtils.getPrimary(method)).factoryMethod(method).factoryName(clazz.getName()).build();
                registry.registerBeanDefinition(beanDefinition.getBeanName(), beanDefinition);
            }
        }
        // 1 check other path
        ComponentScan componentScan = null;
        String scanPackage = clazz.getPackageName();
        if ((componentScan = ClassUtils.getAnnotation(clazz, ComponentScan.class)) != null) {
            scanPackage = componentScan.value();
        }
        scanOn(scanPackage, null);
        // 2 enable @Import
        Import imports = null;
        if ((imports = ClassUtils.getAnnotation(clazz, Import.class)) != null) {
            for (Class<?> importClass : imports.value()) {
                scanOn(importClass);
            }
        }
        // 3 load properties
        PropertySource propertySource = null;
        if ((propertySource = ClassUtils.getAnnotation(clazz, PropertySource.class)) != null) {
            String[] locations = propertySource.value();
            for (String location : locations) {
                parseConfig(location);
            }
        }
    }

    private void parseConfig(String location) throws IOException, URISyntaxException {
        logger.debug("Parsing config file: {}", location);
        Resource resource = ResourceUtils.getResource(location);
        if (resource == null) {
            return;
        }
        if (resource.getType() == ResourceType.YAML_CONFIG) {
            var map = YamlUtils.flattenYaml(YamlUtils.parseYaml(resource.getInputStream()));
            for (var key : map.keySet()) {
                propertyRegistry.set(key, map.get(key));
            }
        } else if (resource.getType() == ResourceType.PROPERTY_CONFIG) {
            parseProperties(resource.getInputStream());
        }
    }

    private BeanDefinition buildBeanDefinition(Class<?> clazz) throws NoSuchMethodException {
        logger.debug("Building bean definition for class: {}", clazz.getName());
        Component component = ClassUtils.getAnnotation(clazz, Component.class);
        if (component != null) {
            String beanName = null;
            if (!StringUtils.isEmpty(component.value())) {
                beanName = component.value();
            } else {
                beanName = StringUtils.setHeadLower(clazz.getSimpleName());
            }
            BeanDefinition beanDefinition = BeanDefinition.builder().beanName(beanName).declaredClass(clazz)
                    .initMethod(null).destroyMethod(null)
                    .initMethod(ClassUtils.getAnnotationMethod(clazz, PostConstruct.class))
                    .destroyMethod(ClassUtils.getAnnotationMethod(clazz, BeforeDestroy.class))
                    .constructor(ClassUtils.getSuitableConstructor(clazz)).order(ClassUtils.getOrder(clazz))
                    .primary(ClassUtils.getPrimary(clazz)).build();
            return beanDefinition;
        }
        return null;
    }

    private void parseYaml(InputStream is) throws IOException {
        if (is == null) {
            return;
        }
        try (BufferedReader bfr = new BufferedReader(new InputStreamReader(is));) {
            String line;
            while ((line = bfr.readLine()) != null) {
                int index = line.indexOf("=");
                String key = line.substring(0, index);
                String value = line.substring(index + 1);
                propertyRegistry.set(key, value);
            }
        }
    }

    private void parseProperties(InputStream is) throws IOException {
        if (is == null) {
            return;
        }
        try (BufferedReader bfr = new BufferedReader(new InputStreamReader(is));) {
            String line;
            while ((line = bfr.readLine()) != null) {
                int index = line.indexOf("=");
                String key = line.substring(0, index);
                String value = line.substring(index + 1);
                propertyRegistry.set(key, value);
            }
        }
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
}
