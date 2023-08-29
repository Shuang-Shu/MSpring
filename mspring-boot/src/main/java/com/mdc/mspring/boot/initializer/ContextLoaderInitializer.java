package com.mdc.mspring.boot.initializer;

import com.mdc.mspring.context.factory.ConfigurableApplicationContext;
import com.mdc.mspring.context.factory.impl.AnnotationConfigApplicationContext;
import com.mdc.mspring.mvc.config.WebMvcConfiguration;
import com.mdc.mspring.mvc.servlet.DispatcherServlet;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Set;

public class ContextLoaderInitializer implements ServletContainerInitializer {
    private static final Logger logger = LoggerFactory.getLogger(ContextLoaderInitializer.class);

    private Class<?> configClass;

    public ContextLoaderInitializer(Class<?> configClass) {
        this.configClass = configClass;
    }

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        logger.info("ContextLoaderInitializer initializing...");
        ConfigurableApplicationContext applicationContext = null;
        WebMvcConfiguration.setServletContext(ctx);
        try {
            applicationContext = createApplicationContext(ctx);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException
                 | ClassNotFoundException | IOException | URISyntaxException e) {
            logger.error(e.getMessage());
        }
        logger.info("Application context initialized: {}", applicationContext);
        // 实例化DispatcherServlet:
        var dispatcherServlet = new DispatcherServlet(applicationContext);
        // 获取servletContext
        var servletContext = ctx;
        // 注册DispatcherServlet:
        var dispatcherReg = servletContext.addServlet("dispatcherServlet", dispatcherServlet);
        logger.info("DispatcherServlet: {} registered with name: {}", dispatcherServlet, "dispatcherServlet");
        dispatcherReg.addMapping("/");
        dispatcherReg.setLoadOnStartup(0);
        // 将IoC容器注入到tomcat容器中
        servletContext.setAttribute("applicationContext", applicationContext);
    }

    private ConfigurableApplicationContext createApplicationContext(ServletContext ctx)
            throws IOException, URISyntaxException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        logger.info("Initializing AnnotationConfigApplicationContext...");
        // 读取配置类:
        String configClassName = ctx.getInitParameter("configuration");
        Class<?> configClass = this.configClass;
        return new AnnotationConfigApplicationContext(configClass);
    }
}
