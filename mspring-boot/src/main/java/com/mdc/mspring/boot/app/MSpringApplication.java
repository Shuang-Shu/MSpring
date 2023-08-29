package com.mdc.mspring.boot.app;

import com.mdc.mspring.boot.initializer.ContextLoaderInitializer;
import com.mdc.mspring.context.resolver.ResourceResolver;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

public class MSpringApplication {
    public static void run(String webDir, String baseDir, Class<?> configClass, String... args) throws IOException, URISyntaxException, ClassNotFoundException, LifecycleException {
        var resourceResolver = new ResourceResolver();
        var server = startTomcat(webDir, baseDir, configClass, resourceResolver);
        server.await();
    }

    private static Server startTomcat(String webDir, String baseDir, Class<?> configClass, ResourceResolver resourceResolver) throws LifecycleException {
        int port = Integer.parseInt(resourceResolver.getProperty("${server.port:8080}"));
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        // 设置Connector
        tomcat.getConnector().setThrowOnFailure(true);
        // 添加默认的webapp
        Context ctx = tomcat.addContext("", new File(webDir).getAbsolutePath());
        // 设置应用程序目录
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(
                new DirResourceSet(resources, "/WEB-INF/classes", new File(baseDir).getAbsolutePath(), "/")
        );
        ctx.addServletContainerInitializer(new ContextLoaderInitializer(configClass), Set.of());
        tomcat.start();
        return tomcat.getServer();
    }
}
