package com.mdc.mspring.aop.processor;

import com.mdc.mspring.aop.exception.AopException;
import com.mdc.mspring.aop.resolver.ProxyResolver;
import com.mdc.mspring.context.annotation.Autowired;
import com.mdc.mspring.context.annotation.Aware;
import com.mdc.mspring.context.entity.ioc.BeanPostProcessor;
import com.mdc.mspring.context.factory.support.BeanFactory;
import com.mdc.mspring.context.factory.support.ListableBeanFactory;
import com.mdc.mspring.context.factory.support.BeanDefinition;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/14/14:26
 * @Description:
 */
public class ProxyBeanPostProcessor<A extends Annotation> extends BeanPostProcessor implements Aware {
    private BeanFactory beanFactory;
    @Autowired
    private ProxyResolver resolver;
    private final Class<A> classA;

    public ProxyBeanPostProcessor() {
        this.classA = getParameterizedType();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (resolver == null) {
            BeanDefinition resolverDefinition = (BeanDefinition) ((ListableBeanFactory) beanFactory).getBeanDefinition(ProxyResolver.class);
            if (resolverDefinition.getInstance() == null) {
                ((ListableBeanFactory) beanFactory).createBeanAsEarlySingleton(resolverDefinition);
            }
            resolver = (ProxyResolver) resolverDefinition.getInstance();
        }
        Annotation annotation = bean.getClass().getAnnotation(this.classA);
        Object proxy = bean;
        String handlerName;
        if (annotation != null) {
            try {
                handlerName = (String) annotation.annotationType().getMethod("value").invoke(annotation);
            } catch (ReflectiveOperationException e) {
                throw new AopException(String.format("@%s must have value() returned String type.", this.classA.getSimpleName()), e);
            }
            InvocationHandler invocationHandler = beanFactory.getBean(handlerName, InvocationHandler.class);
            if (invocationHandler == null) {
                invocationHandler = (InvocationHandler) ((ListableBeanFactory) beanFactory).createBeanAsEarlySingleton(((ListableBeanFactory) beanFactory).getBeanDefinition(handlerName));
            }
            proxy = resolver.createProxy(bean, invocationHandler);
        }
        return proxy;
    }

    @Override
    public void setContext(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @SuppressWarnings("unchecked")
    private Class<A> getParameterizedType() {
        Type type = getClass().getGenericSuperclass();
        if (!(type instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Class " + getClass().getName() + " does not have parameterized type.");
        }
        ParameterizedType pt = (ParameterizedType) type;
        Type[] types = pt.getActualTypeArguments();
        if (types.length != 1) {
            throw new IllegalArgumentException("Class " + getClass().getName() + " has more than 1 parameterized types.");
        }
        Type r = types[0];
        if (!(r instanceof Class<?>)) {
            throw new IllegalArgumentException("Class " + getClass().getName() + " does not have parameterized type of class.");
        }
        return (Class<A>) r;
    }
}
