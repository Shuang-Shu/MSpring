package com.mdc.mspring.app.processor;

import com.mdc.mspring.app.service.TestService;
import com.mdc.mspring.app.service.TestServicePorxy;
import com.mdc.mspring.context.entity.ioc.BeanPostProcessor;
import com.mdc.mspring.context.anno.Component;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/13/17:21
 * @Description:
 */
@Component
public class TestBeanPostProcessor extends BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof TestService) {
            return new TestServicePorxy((TestService) bean);
        }
        return bean;
    }
}
