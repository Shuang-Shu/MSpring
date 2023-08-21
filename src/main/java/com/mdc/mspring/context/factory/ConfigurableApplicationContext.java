package com.mdc.mspring.context.factory;

import com.mdc.mspring.context.anno.Nullable;
import com.mdc.mspring.context.entity.ioc.BeanDefinition;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/14/1:40
 * @Description:
 */
public interface ConfigurableApplicationContext extends Context {
    List<BeanDefinition> findBeanDefinitions(Class<?> type);

    @Nullable
    BeanDefinition findBeanDefinition(Class<?> type);

    @Nullable
    BeanDefinition findBeanDefinition(String name);

    @Nullable
    BeanDefinition findBeanDefinition(String name, Class<?> requiredType);

    List<BeanDefinition> getBeanDefinitionsByAnnotation(Class<? extends Annotation> anno);

    Object createBeanAsEarlySingleton(BeanDefinition def);
}
