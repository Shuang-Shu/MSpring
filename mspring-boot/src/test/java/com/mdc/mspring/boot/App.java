package com.mdc.mspring.boot;

import com.mdc.mspring.boot.app.MSpringApplication;
import com.mdc.mspring.boot.config.TestAppConfig;
import org.apache.catalina.LifecycleException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

public class App {
    public static void main(String[] args) throws LifecycleException, IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        MSpringApplication.run("src/main/webapp", "target/classes", TestAppConfig.class);
    }
}
