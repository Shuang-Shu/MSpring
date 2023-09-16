package com.mdc.mspring.context.test.config.bean;

import com.mdc.mspring.context.annotation.Component;
import com.mdc.mspring.context.annotation.Value;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/16 15:11
 */
@Component
public class Bean1 {
    @Value("bean1.test")
    public String name;
}
