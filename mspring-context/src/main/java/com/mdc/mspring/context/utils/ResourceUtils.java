package com.mdc.mspring.context.utils;

import com.mdc.mspring.context.common.ResourceType;
import com.mdc.mspring.context.io.DefaultResource;
import com.mdc.mspring.context.io.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/15 15:51
 */
public class ResourceUtils {
    private final static Logger logger = LoggerFactory.getLogger(ResourceUtils.class);

    public static List<Resource> getAllResources(String path, ClassLoader classLoader) throws IOException, URISyntaxException, ClassNotFoundException {
        List<Resource> urls = new ArrayList<>();
        Set<Resource> urlSet = new HashSet<>();
        doGetAllResources(path, classLoader, urlSet);
        urls.addAll(urlSet);
        return urls;
    }

    public static List<Resource> getAllResources(String path) throws IOException, URISyntaxException, ClassNotFoundException {
        return getAllResources(path, Thread.currentThread().getContextClassLoader());
    }

    private static void doGetAllResources(String path, ClassLoader classLoader, Set<Resource> urlSet) throws IOException, URISyntaxException, ClassNotFoundException {
        var resources = classLoader.getResources(path);
        while (resources.hasMoreElements()) {
            var resource = resources.nextElement();
            var uri = resource.toURI();
            if (uri.toString().startsWith("file:")) {
                urlSet.addAll(getResourcesFromFile(new File(uri), path));
            } else if (uri.toString().startsWith("jar:")) {
                JarFile jarFile = ((JarURLConnection) resource.openConnection()).getJarFile();
                urlSet.addAll(getResourcesFromJar(jarFile, path));
            }
        }
    }

    private static List<Resource> getResourcesFromFile(File file, String basePackage) throws IOException, ClassNotFoundException {
        List<Resource> result = new ArrayList<>();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            assert files != null;
            for (File f : files) {
                result.addAll(getResourcesFromFile(f, basePackage));
            }
        } else {
            String suffix = file.getPath().substring(file.getPath().lastIndexOf(".") + 1);
            ResourceType type = switch (suffix) {
                case "class" -> ResourceType.CLASS;
                case "properties" -> ResourceType.PROPERTY_CONFIG;
                case "yml", "yaml" -> ResourceType.YAML_CONFIG;
                default -> ResourceType.OTHER;
            };
            String name = file.getPath().substring(file.getPath().indexOf(basePackage.replace(".", "/")));
            var is = new FileInputStream(file);
            result.add(DefaultResource.builder().url(file.toURI().toURL()).name(name).type(type).inputStream(is).build());
        }
        return result;
    }

    private static List<Resource> getResourcesFromJar(JarFile jar, String basePackage) throws ClassNotFoundException, MalformedURLException {
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
            ResourceType type = switch (suffix) {
                case "class" -> ResourceType.CLASS;
                case "properties" -> ResourceType.PROPERTY_CONFIG;
                case "yml", "yaml" -> ResourceType.YAML_CONFIG;
                default -> ResourceType.OTHER;
            };
            InputStream is = null;
            try {
                is = jar.getInputStream(jarEntry);
            } catch (IOException e) {
                is = null;
            }
            URL url = new URL("jar:file:" + jar.getName() + "!/" + jar.getName());
            result.add(DefaultResource.builder().url(url).name(name).type(type).inputStream(is).build());
        }
        return result;
    }

    public static Resource getResource(String location) throws URISyntaxException, FileNotFoundException {
        var classLoader = Thread.currentThread().getContextClassLoader();
        var url = classLoader.getResource(location);
        if (url == null) {
            return null;
        }
        var uri = url.toURI();
        ResourceType resourceType = switch (uri.toString().substring(uri.toString().lastIndexOf(".") + 1)) {
            case "class" -> ResourceType.CLASS;
            case "properties" -> ResourceType.PROPERTY_CONFIG;
            case "yml", "yaml" -> ResourceType.YAML_CONFIG;
            default -> ResourceType.OTHER;
        };
        InputStream is = null;
        if (uri.toString().startsWith("file:")) {
            try {
                is = new FileInputStream(new File(uri));
            } catch (FileNotFoundException e) {
                logger.error("Can not find file: " + uri);
                e.printStackTrace();
            }
        } else if (uri.toString().startsWith("jar:")) {
            JarFile jarFile = null;
            try {
                jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
            } catch (IOException e) {
                logger.error("Can not find file: " + uri);
                e.printStackTrace();
            }
            Enumeration<JarEntry> entries = jarFile.entries();
            JarEntry jarEntry;
            while (entries.hasMoreElements()) {
                jarEntry = entries.nextElement();
                if (jarEntry.getName().equals(location)) {
                    try {
                        is = jarFile.getInputStream(jarEntry);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        return DefaultResource.builder().type(resourceType).name(location).url(url).inputStream(is).build();
    }
}
