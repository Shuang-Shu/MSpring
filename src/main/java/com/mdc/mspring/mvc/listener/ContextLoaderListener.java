package com.mdc.mspring.mvc.listener;

import com.mdc.mspring.context.factory.ConfigurableApplicationContext;
import com.mdc.mspring.context.factory.impl.AnnotationConfigApplicationContext;
import com.mdc.mspring.context.resolver.PropertyResolver;
import com.mdc.mspring.context.resolver.ResourceResolver;
import com.mdc.mspring.mvc.config.WebMvcConfiguration;
import com.mdc.mspring.mvc.servlet.DispatcherServlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/21/18:41
 * @Description:
 */
public class ContextLoaderListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 创建IoC容器:
        ConfigurableApplicationContext applicationContext = null;
        try {
            applicationContext = createApplicationContext(sce);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException
                 | ClassNotFoundException | IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        // 实例化DispatcherServlet:
        var dispatcherServlet = new DispatcherServlet();
        // 获取servletContext
        var servletContext = sce.getServletContext();
        // 注册DispatcherServlet:
        var dispatcherReg = servletContext.addServlet("dispatcherServlet", dispatcherServlet);
        dispatcherReg.addMapping("/");
        dispatcherReg.setLoadOnStartup(0);
        // 将IoC容器注入到tomcat容器中
        servletContext.setAttribute("applicationContext", applicationContext);
    }

    private ConfigurableApplicationContext createApplicationContext(ServletContextEvent sce)
            throws IOException, URISyntaxException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        ResourceResolver resolver = new ResourceResolver("com.mdc");
        PropertyResolver propertyResolver = new PropertyResolver();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                WebMvcConfiguration.class, resolver, propertyResolver);
        return applicationContext;
    }
}
