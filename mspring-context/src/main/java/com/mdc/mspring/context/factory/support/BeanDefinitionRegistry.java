package com.mdc.mspring.context.factory.support;

import com.mdc.mspring.context.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;

public interface BeanDefinitionRegistry {
    void registerBeanDefinition(String name, BeanDefinition beanDefinition);

    void removeBeanDefinition(String name);

    BeanDefinition getBeanDefinition(String name);

    BeanDefinition getBeanDefinition(Class<?> type);

    List<BeanDefinition> getBeanDefinitions(Class<?> type);

    @Nullable
    BeanDefinition getBeanDefinition(String name, Class<?> requiredType);

    boolean containsBeanDefinition(String name);

    String[] getBeanDefinitionNames();

    int getBeanDefinitinoCount();

    List<BeanDefinition> getBeanDefinitionsByAnnotation(Class<? extends Annotation> anno);
}
