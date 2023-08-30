package com.mdc.mspring.boot.webapp;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mdc.mspring.boot.app.MSpringApplication;
import com.mdc.mspring.boot.webapp.config.AppConfig;

public class App {
    public static void printPath(String name) {
        System.out.println(name + ":");
        String prop = System.getProperty(name);
        if (prop != null) {
            String[] paths = prop.split(File.pathSeparator);
            for (String path : paths) {
                System.out.println("- " + path);
            }
        }
    }

    public static void printAll() {
        printPath("java.home");
        printPath("sun.boot.class.path");
        printPath("java.ext.dirs");
        printPath("java.class.path");
    }

    public static void main(String[] args)
            throws ClassNotFoundException, IOException, URISyntaxException {
        printAll();
        System.out.println("\n===\n\n");
        String jarFile = App.class.getResource("App.class").toURI().toString();
        boolean isJarFile = jarFile.startsWith("jar") || jarFile.endsWith("war");
        if (jarFile.startsWith("jar")) {
            jarFile = jarFile.substring("jar:file:".length());
            jarFile = jarFile.substring(0, jarFile.indexOf("!"));
        }
        System.out.println("isJarFile: " + isJarFile + ", jarFile=" + jarFile);
        System.out.println("\n\n" + App.class.getResource("").toURI().toString());
        // 设置webapp根目录
        String webDir = isJarFile ? "tmp-webapp" : "src/main/webapp";
        if (isJarFile) {
            // 解压到tmp-webapp:
            Path baseDir = Paths.get(webDir).normalize().toAbsolutePath();
            if (Files.isDirectory(baseDir)) {
                Files.delete(baseDir);
            }
            Files.createDirectories(baseDir);
            System.out.println("extract to: " + baseDir);
            try (JarFile jar = new JarFile(jarFile)) {
                List<JarEntry> entries = jar.stream().sorted(Comparator.comparing(JarEntry::getName))
                        .collect(Collectors.toList());
                for (JarEntry entry : entries) {
                    Path res = baseDir.resolve(entry.getName());
                    if (!entry.isDirectory()) {
                        System.out.println(res);
                        Files.createDirectories(res.getParent());
                        Files.copy(jar.getInputStream(entry), res);
                    }
                }
            }
            // JVM退出时自动删除tmp-webapp:
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Files.walk(baseDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        }
        // 基于启动项目的War包创建的对应的类加载器

        MSpringApplication.run(webDir, isJarFile ? "tmp-webapp" : "target/classes", AppConfig.class);
    }
}
