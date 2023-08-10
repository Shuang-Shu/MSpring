package com.mdc.mspring.resolver;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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
    Logger logger = LoggerFactory.getLogger(getClass());
    String basePackage; // format: com.example.demo

    public ResourceResolver(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * @Description: Do scanning in basePackage, all scanned files will be repersented as Resource objects. The mapper will process them.
     * @Param:
     * @Return:
     * @Author: ShuangShu
     * @Date:
     */
    public <R> List<R> scan(Function<Resource, R> mapper) throws IOException, URISyntaxException {
        // get all files in classpath/basePackage
        String packagePath = basePackage.replace(".", "/");
        List<Resource> resourceCollector = new ArrayList<>();
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(packagePath);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            URI uri = resource.toURI();

            if (uri.toString().startsWith("file:")) {
                resourceCollector.addAll(getResourcesFromFile(new File(uri)));
            } else if (uri.toString().startsWith("jar:")) {
                JarFile jarFile = ((JarURLConnection) resource.openConnection()).getJarFile();
                resourceCollector.addAll(getResourcesFromJar(jarFile, uri, packagePath));
            }
        }
        return resourceCollector.stream().map(mapper).collect(Collectors.toList());
    }

    /**
      * @Description: Get resources from file
      * @Param: 
      * @Return: 
      * @Author: ShuangShu
      * @Date: 2023/8/10 18:20
      */
    private List<Resource> getResourcesFromFile(File file) throws IOException {
        List<Resource> result = new ArrayList<>();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            assert files != null;
            for (File f : files) {
                result.addAll(getResourcesFromFile(f));
            }
        } else {
            String fullName = file.getPath().substring(file.getPath().indexOf(basePackage.replace(".", "/")));
            result.add(new Resource("file:" + file.getPath(), fullName));
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
    private List<Resource> getResourcesFromJar(JarFile jar, URI uri, String basePackage) {
        List<Resource> result = new ArrayList<>();
        Enumeration<JarEntry> entries = jar.entries();
        JarEntry jarEntry;
        String name, className;
        while (entries.hasMoreElements()) {
            jarEntry = entries.nextElement();
            name = jarEntry.getName();
            if (name.startsWith("/")) {
                name = name.substring(1);
            }
            if (jarEntry.isDirectory() || !name.startsWith(basePackage) || !name.endsWith(".class")) {
                continue;
            }
            String uriStr = uri.toString();
            result.add(new Resource(uriStr.substring(0, uriStr.length() - basePackage.length() - 1), name));
        }
        return result;
    }
}
