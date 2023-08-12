package com.mdc.mspring.context.impl;

import com.mdc.mspring.anno.ioc.*;
import com.mdc.mspring.context.Context;
import com.mdc.mspring.entity.Resource;
import com.mdc.mspring.entity.ioc.BeanDefinition;
import com.mdc.mspring.exception.BeanCreateException;
import com.mdc.mspring.exception.BeanDefinitionException;
import com.mdc.mspring.exception.DuplicatedBeanNameException;
import com.mdc.mspring.exception.NoUniqueBeanDefinitionException;
import com.mdc.mspring.resolver.ioc.PropertyResolver;
import com.mdc.mspring.resolver.ioc.ResourceResolver;
import com.mdc.mspring.utils.ClassUtils;
import com.mdc.mspring.utils.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/11/15:28
 * @Description:
 */
public class AnnotationConfigApplicationContext implements Context {
    // save beanDefinition objects
    private Map<String, BeanDefinition> beans = new HashMap<>();

    public AnnotationConfigApplicationContext(Class<?> confgClass, ResourceResolver resourceResolver, PropertyResolver propertyResolver) throws IOException, URISyntaxException, NoSuchMethodException {
        // 1 get class names in scanPackage
        String scanPackage = ((ComponentScan) ClassUtils.getAnnotation(confgClass, ComponentScan.class, new HashSet<>())).value();
        List<String> classNames = resourceResolver.scan(scanPackage, Resource::name, ResourceResolver.CLASS_SUFFIX, -1).stream().map(n -> {
            n = n.replace('/', '.');
            return n.substring(0, n.length() - 6);
        }).toList();
        // 2 create BeanDefinitions
        this.beans = createBeanDefinitions(classNames);
    }

    private Map<String, BeanDefinition> createBeanDefinitions(List<String> classNames) throws NoSuchMethodException {
        Map<String, BeanDefinition> result = new HashMap<>();
        for (String className : classNames) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new BeanCreateException("class: " + classNames + " not found");
            }
            Component component = (Component) ClassUtils.getAnnotation(clazz, Component.class, new HashSet<>());
            if (component != null) {
                String beanName = null;
                if (!StringUtils.isEmptyString(component.value())) {
                    beanName = component.value();
                } else {
                    beanName = StringUtils.simpleName(className);
                }
                BeanDefinition beanDefinition = BeanDefinition.builder().beanName(beanName).declaredClass(clazz).initMethod(null).destroyMethod(null).initMethod(ClassUtils.getAnnotationMethod(clazz, PostConstruct.class)).destroyMethod(ClassUtils.getAnnotationMethod(clazz, BeforeDestroy.class)).constructor(ClassUtils.getSuitableConstructor(clazz)).order(ClassUtils.getOrder(clazz)).primary(ClassUtils.getPrimary(clazz)).build();

                result.put(beanName, beanDefinition);
                if (ClassUtils.getAnnotation(clazz, Configuration.class, new HashSet<>()) != null) {
                    addBeanFactoryDefinition(clazz, result);
                }
            }
        }
        return result;
    }

    private void addBeanFactoryDefinition(Class<?> clazz, Map<String, BeanDefinition> result) throws NoSuchMethodException {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getAnnotation(Bean.class) != null) {
                Bean bean = method.getAnnotation(Bean.class);
                Class<?> returnType = method.getReturnType();
                BeanDefinition beanDefinition = BeanDefinition.builder().beanName(StringUtils.isEmptyString(bean.value()) ? StringUtils.getLowerCase(returnType.getName()) : bean.value()).declaredClass(returnType).constructor(ClassUtils.getSuitableConstructor(returnType)).factoryMethod(method).factoryName(method.getName()).order(ClassUtils.getOrderMethod(method)).primary(ClassUtils.getPrimaryMethod(method)).initMethodName(bean.initMethod()).destroyMethodName(bean.destroyMethod()).build();
                result.put(beanDefinition.getBeanName(), beanDefinition);
            }
        }
    }

    /**
     * @Description: Find method in clazz type which is annotationed by annotationClass
     * @Param:
     * @Return:
     * @Author: ShuangShu
     * @Date: 2023/8/11 22:34
     */
    @Override
    public void register(BeanDefinition definition) {
        if (beans.containsKey(definition.getBeanName())) {
            throw new DuplicatedBeanNameException("duplicated bean names found: " + definition.getBeanName());
        }
    }

    public BeanDefinition findDefinition(String name) {
        return beans.get(name);
    }

    public List<BeanDefinition> findDefinitions(Class<?> clazz) {
        return beans.values().stream().filter(
                bd -> {
                    return clazz.isAssignableFrom(bd.getDeclaredClass());
                }
        ).sorted().collect(Collectors.toList());
    }

    public BeanDefinition findDefinition(Class<?> clazz) {
        List<BeanDefinition> definitions = findDefinitions(clazz);
        List<BeanDefinition> primaryDefs = definitions.stream().filter(
                BeanDefinition::isPrimary
        ).toList();
        if (primaryDefs.size() != 1) {
            throw new NoUniqueBeanDefinitionException("no/more than 1 @Primary specifed beans are found of type: " + clazz.getName());
        } else {
            return primaryDefs.get(0);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String beanName, Class<T> clazz) {
        BeanDefinition definition = findDefinition(beanName);
        return (T) definition.getInstance();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        return (T) findDefinition(clazz).getInstance();
    }
}
