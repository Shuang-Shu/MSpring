package com.mdc.mspring.context.test.config.bean3;

import com.mdc.mspring.context.annotation.Component;
import com.mdc.mspring.context.annotation.Value;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/16 15:12
 */
@Component
public class Bean3 {
    @Value("bean3.test")
    public String name;
}
