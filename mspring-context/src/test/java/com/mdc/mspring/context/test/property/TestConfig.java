package com.mdc.mspring.context.test.property;

import com.mdc.mspring.context.annotation.ComponentScan;
import com.mdc.mspring.context.annotation.Configuration;
import com.mdc.mspring.context.annotation.PropertySource;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/16 9:20
 */
@Configuration
@ComponentScan("com.mdc.mspring.context.test.property")
@PropertySource("config/config.yaml")
public class TestConfig {
}
