package com.mdc.mspring.context.factory.support;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/15 18:55
 */
public abstract class AbstractApplicationContext implements ListableBeanFactory, PropertyRegistry {
    public abstract PropertyRegistry getPropertyRegistry();
}
