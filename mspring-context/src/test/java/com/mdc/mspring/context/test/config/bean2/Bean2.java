package com.mdc.mspring.context.test.config.bean2;

import com.mdc.mspring.context.annotation.Component;
import com.mdc.mspring.context.annotation.Value;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/16 15:10
 */
@Component
public class Bean2 {
    @Value("bean2.test")
    public String name;
}
