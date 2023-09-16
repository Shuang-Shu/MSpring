package com.mdc.mspring.context.factory.support.impl;

import com.mdc.mspring.context.annotation.*;
import com.mdc.mspring.context.exception.BeanDefinitionException;
import com.mdc.mspring.context.factory.support.BeanDefinition;
import com.mdc.mspring.context.factory.support.BeanDefinitionRegistry;
import com.mdc.mspring.context.utils.ClassUtils;
import com.mdc.mspring.context.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/13 20:38
 */
public class DefaultBeanDefinitionRegistry implements BeanDefinitionRegistry {
    private static final Logger logger = LoggerFactory.getLogger(DefaultBeanDefinitionRegistry.class);

    private final Map<String, BeanDefinition> definitionMap = new ConcurrentHashMap<>();

    @Override
    public void registerBeanDefinition(String name, BeanDefinition beanDefinition) {
        if (definitionMap.containsKey(name)) {
            return;
//            logger.error("duplicate registration: {}", name);
//            throw new BeanDefinitionException("duplicate registration");
        }
        definitionMap.put(name, beanDefinition);
    }

    @Override
    public void removeBeanDefinition(String name) {
        definitionMap.remove(name);
    }

    @Override
    public BeanDefinition getBeanDefinition(String name) {
        return definitionMap.get(name);
    }

    @Override
    public BeanDefinition getBeanDefinition(Class<?> type) {
        for (var definition : definitionMap.values()) {
            if (definition.getDeclaredClass() == type) {
                return definition;
            }
        }
        return null;
    }

    @Override
    public List<BeanDefinition> getBeanDefinitions(Class<?> type) {
        if (type == null) {
            throw new BeanDefinitionException("type cannot be null");
        }
        return definitionMap.values().stream().filter(t -> type.isAssignableFrom(t.getDeclaredClass())).sorted(
                new Comparator<BeanDefinition>() {
                    @Override
                    public int compare(BeanDefinition bd1, BeanDefinition bd2) {
                        return bd1.getOrder() - bd2.getOrder();
                    }
                }
        ).toList();
    }

    @Override
    public BeanDefinition getBeanDefinition(String name, Class<?> requiredType) {
        return null;
    }

    @Override
    public boolean containsBeanDefinition(String name) {
        return definitionMap.containsKey(name);
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return definitionMap.keySet().toArray(new String[0]);
    }

    @Override
    public int getBeanDefinitinoCount() {
        return definitionMap.size();
    }

    @Override
    public List<BeanDefinition> getBeanDefinitionsByAnnotation(Class<? extends Annotation> anno) {
        return definitionMap.values().stream().filter(
                bd -> {
                    return ClassUtils.getAnnotation(bd.getDeclaredClass(), anno) != null;
                }).toList();
    }

//    public void buildBeanDefintionsByClassNames(String[] names) throws NoSuchMethodException {
//        buildBeanDefinitionsByClasses(
//                Arrays.stream(names).map(n -> {
//                    try {
//                        return Class.forName(n);
//                    } catch (ClassNotFoundException e) {
//                        throw new RuntimeException(e);
//                    }
//                }).toList().toArray(new Class<?>[0])
//        );
//    }

    private void addBeanFactoryDefinition(Class<?> clazz)
            throws NoSuchMethodException {
        // Method[] methods = clazz.getMethods();
        Set<Method> methodSet = new HashSet<>();
        methodSet.addAll(Arrays.asList(clazz.getMethods()));
        methodSet.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        for (Method method : methodSet) {
            if (method.getAnnotation(Bean.class) != null) {
                Bean bean = method.getAnnotation(Bean.class);
                Class<?> returnType = method.getReturnType();
                BeanDefinition beanDefinition = BeanDefinition.builder()
                        .beanName(StringUtils.isEmpty(bean.value())
                                ? StringUtils.getFirstCharLowerCase(StringUtils.simpleName(returnType.getName()))
                                : bean.value())
                        .declaredClass(returnType).factoryMethod(method).factoryName(clazz.getName())
                        .order(ClassUtils.getOrder(method)).primary(ClassUtils.getPrimary(method))
                        .initMethodName(bean.initMethod()).destroyMethodName(bean.destroyMethod()).build();
                definitionMap.put(beanDefinition.getBeanName(), beanDefinition);
            }
        }
    }
}
