package com.mdc.mspring.aop.processor;

import com.mdc.mspring.aop.anno.Around;
import com.mdc.mspring.aop.resolver.ProxyResolver;
import com.mdc.mspring.context.anno.Autowired;
import com.mdc.mspring.context.anno.Aware;
import com.mdc.mspring.context.anno.Component;
import com.mdc.mspring.context.entity.ioc.BeanDefinition;
import com.mdc.mspring.context.entity.ioc.BeanPostProcessor;
import com.mdc.mspring.context.factory.ConfigurableApplicationContext;
import com.mdc.mspring.context.factory.Context;

import java.lang.reflect.InvocationHandler;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/14/14:26
 * @Description:
 */
@Component
public class EnhancedBeanProcessor extends BeanPostProcessor implements Aware {
    private Context context;
    @Autowired
    private ProxyResolver resolver;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (resolver == null) {
            BeanDefinition resolverDefinition = (BeanDefinition) ((ConfigurableApplicationContext) context).findBeanDefinition(ProxyResolver.class);
            if (resolverDefinition.getInstance() == null) {
                ((ConfigurableApplicationContext) context).createBeanAsEarlySingleton(resolverDefinition);
            }
            resolver = (ProxyResolver) resolverDefinition.getInstance();
        }
        Object proxy = bean;
        Around around = bean.getClass().getAnnotation(Around.class);
        if (around != null) {
            InvocationHandler invocationHandler = context.getBean(around.value(), InvocationHandler.class);
            if (invocationHandler == null) {
                invocationHandler = (InvocationHandler) ((ConfigurableApplicationContext) context).createBeanAsEarlySingleton(((ConfigurableApplicationContext) context).findBeanDefinition(around.value()));
            }
            proxy = resolver.createProxy(bean, invocationHandler);
        }
        return proxy;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }
}
