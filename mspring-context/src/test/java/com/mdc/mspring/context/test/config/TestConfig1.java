package com.mdc.mspring.context.test.config;

import com.mdc.mspring.context.annotation.ComponentScan;
import com.mdc.mspring.context.annotation.Configuration;
import com.mdc.mspring.context.annotation.Import;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/16 15:09
 */
@Configuration
@ComponentScan("com.mdc.mspring.context.test.config.bean")
@Import(TestConfig2.class)
public class TestConfig1 {
}
