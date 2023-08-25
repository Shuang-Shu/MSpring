package com.mdc.mspring.context.entity.ioc;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/13/16:18
 * @Description:
 */
public abstract class BeanPostProcessor {
    public abstract Object postProcessBeforeInitialization(Object bean, String beanName);
}
