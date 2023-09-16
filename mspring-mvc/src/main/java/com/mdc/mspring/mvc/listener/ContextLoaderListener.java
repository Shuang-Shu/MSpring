package com.mdc.mspring.mvc.listener;

import com.mdc.mspring.context.factory.support.AbstractApplicationContext;
import com.mdc.mspring.context.factory.support.ListableBeanFactory;
import com.mdc.mspring.context.factory.AnnotationConfigApplicationContext;
import com.mdc.mspring.mvc.config.WebMvcConfiguration;
import com.mdc.mspring.mvc.servlet.DispatcherServlet;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(ContextLoaderListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("ContextLoaderListener initializing...");
        // 创建IoC容器:
        AbstractApplicationContext applicationContext = null;
        WebMvcConfiguration.setServletContext(sce.getServletContext());
        try {
            applicationContext = createApplicationContext(sce);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException
                | ClassNotFoundException | IOException | URISyntaxException e) {
            logger.error(e.getMessage());
        }
        logger.info("Application context initialized: {}", applicationContext);
        // 实例化DispatcherServlet:
        var dispatcherServlet = new DispatcherServlet(applicationContext);
        // 获取servletContext
        var servletContext = sce.getServletContext();
        // 注册DispatcherServlet:
        var dispatcherReg = servletContext.addServlet("dispatcherServlet", dispatcherServlet);
        logger.info("DispatcherServlet: {} registered with name: {}", dispatcherServlet, "dispatcherServlet");
        dispatcherReg.addMapping("/");
        dispatcherReg.setLoadOnStartup(0);
        // 将IoC容器注入到tomcat容器中
        servletContext.setAttribute("applicationContext", applicationContext);
    }

    private AbstractApplicationContext createApplicationContext(ServletContextEvent sce)
            throws IOException, URISyntaxException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        logger.info("Initializing AnnotationConfigApplicationContext...");
        // 读取配置类:
        String configClassName = sce.getServletContext().getInitParameter("configuration");
        Class<?> configClass = null;
        try {
            configClass = Class.forName(configClassName);
        } catch (ClassNotFoundException e) {
            try {
                throw new ServletException("Could not load class from init param 'configuration': " + configClassName);
            } catch (ServletException ex) {
                logger.error(ex.getMessage());
            }
        }
        return new AnnotationConfigApplicationContext(configClass);
    }
}
