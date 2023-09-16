package com.mdc.mspring.context.factory.support;

import java.util.Properties;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/15 16:57
 */
public interface PropertyRegistry {
    void set(String key, String value);

    void setProperties(Properties properties);

    String getProperty(String key);
}
