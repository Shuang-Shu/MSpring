package com.mdc.mspring.jdbc.processor;

import com.mdc.mspring.aop.anno.Around;
import com.mdc.mspring.aop.processor.ProxyBeanPostProcessor;
import com.mdc.mspring.aop.resolver.ProxyResolver;
import com.mdc.mspring.context.anno.Autowired;
import com.mdc.mspring.context.anno.Aware;
import com.mdc.mspring.context.entity.ioc.BeanDefinition;
import com.mdc.mspring.context.entity.ioc.BeanPostProcessor;
import com.mdc.mspring.context.factory.ConfigurableApplicationContext;
import com.mdc.mspring.context.factory.Context;
import com.mdc.mspring.jdbc.anno.Transactional;

import java.lang.reflect.InvocationHandler;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/16/9:31
 * @Description:
 */
public class TransactionalBeanPostProcessor extends ProxyBeanPostProcessor<Transactional> {

}
