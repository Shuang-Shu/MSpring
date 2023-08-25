package com.mdc.mspring.context.factory;

import com.mdc.mspring.context.entity.ioc.BeanDefinition;

import java.util.List;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/11/15:28
 * @Description:
 */
public interface Context extends AutoCloseable {
    public void register(BeanDefinition definition);

    public Object getBean(String beanName);

    public <T> T getBean(String beanName, Class<T> clazz);

    public <T> T getBean(Class<T> clazz);

    <T> List<T> getBeans(Class<T> clazz);

    void close();
}
