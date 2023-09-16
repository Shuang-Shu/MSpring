package com.mdc.mspring.aop.test;

import com.mdc.mspring.aop.config.AopConfiguration;
import com.mdc.mspring.context.annotation.ComponentScan;
import com.mdc.mspring.context.annotation.Configuration;
import com.mdc.mspring.context.annotation.Import;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/16 23:37
 */
@Configuration
@ComponentScan("com.mdc.mspring.aop.test")
@Import(AopConfiguration.class)
public class TestConfig {
}
