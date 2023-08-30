package com.mdc.mspring.boot.webapp;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.tomcat.Jar;

public class ClassLoaderTest {
    public static void main(String[] args) throws IOException, URISyntaxException {
        var myLoader = new MyJarClassLoader(null);
        File file = new File("mspring-boot-0.0.1-SNAPSHOT.jar");
        JarFile jarFile = new JarFile(file);
        var enumer = jarFile.entries();
        while (enumer.hasMoreElements()) {
            var jarEntry = enumer.nextElement();
            if (!jarEntry.isDirectory() && jarEntry.getName().endsWith(".class")) {
                var extraData = jarEntry.getExtra();
                var newClass = myLoader.loadClassFrom(extraData);
                System.out.println(newClass);
            }
        }
    }
}

class MyJarClassLoader extends ClassLoader {
    List<Jar> jars;

    public MyJarClassLoader(List<Jar> jars) {
        this.jars = jars;
    }

    public Class<?> loadClassFrom(byte[] data) {
        return defineClass(null, data, 0, data.length);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return null;
    }
}
