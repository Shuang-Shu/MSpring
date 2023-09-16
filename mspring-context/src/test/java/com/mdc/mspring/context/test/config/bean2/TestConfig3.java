package com.mdc.mspring.context.test.config.bean2;

import com.mdc.mspring.context.annotation.ComponentScan;
import com.mdc.mspring.context.annotation.Configuration;
import com.mdc.mspring.context.annotation.PropertySource;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/16 15:12
 */
@Configuration
@ComponentScan("com.mdc.mspring.context.test.config.bean3")
@PropertySource("config/test.yaml")
public class TestConfig3 {
}
