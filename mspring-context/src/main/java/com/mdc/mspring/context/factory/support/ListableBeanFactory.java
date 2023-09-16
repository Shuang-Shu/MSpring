package com.mdc.mspring.context.factory.support;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/14/1:40
 * @Description:
 */
public interface ListableBeanFactory extends BeanFactory, BeanDefinitionRegistry {

    Object createBeanAsEarlySingleton(BeanDefinition def);

}
