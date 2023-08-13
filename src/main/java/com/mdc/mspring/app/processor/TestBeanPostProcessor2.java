package com.mdc.mspring.app.processor;

import com.mdc.mspring.app.dao.TestDao;
import com.mdc.mspring.app.dao.TestDao2;
import com.mdc.mspring.app.dao.TestDaoProxy;
import com.mdc.mspring.app.dao.TestDaoProxy2;
import com.mdc.mspring.context.anno.Component;
import com.mdc.mspring.context.entity.ioc.BeanPostProcessor;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/13/19:49
 * @Description:
 */
@Component
public class TestBeanPostProcessor2 extends BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof TestDao) {
            return new TestDaoProxy((TestDao) bean);
        } else if (bean instanceof TestDao2) {
            return new TestDaoProxy2((TestDao2) bean);
        }
        return bean;
    }
}
