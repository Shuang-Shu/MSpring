package com.mdc.mspring.webapp;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Arrays;

import com.mdc.mspring.context.anno.Import;
import com.mdc.mspring.context.factory.impl.AnnotationConfigApplicationContext;
import com.mdc.mspring.mvc.servlet.DispatcherServlet;
import com.mdc.mspring.webapp.config.WebAppConfig;

public class App {
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, URISyntaxException {
        var webAppAnno = WebAppConfig.class.getAnnotation(Import.class);
        var importClasses = webAppAnno.value();
        for (var iclass : importClasses) {
            System.out.println(Arrays.toString(iclass.getAnnotations()));
        }

    }
}
