package com.mdc.mspring.context.factory.support;

import lombok.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/11/15:10
 * @Description:
 */
@Data
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeanDefinition {
    private String beanName;
    private Class<?> declaredClass;
    private Object instance;
    private Object originInstance; // for proxy
    private Constructor<?> constructor;
    // for instanting an object
    // under @Configuration
    private Method factoryMethod;
    private String factoryName;

    private int order;
    private boolean primary;

    // init/destroy method
    private Method initMethod;
    private Method destroyMethod;

    // init/destroy method names
    private String initMethodName;
    private String destroyMethodName;
}
