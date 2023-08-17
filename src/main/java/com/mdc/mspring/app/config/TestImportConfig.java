package com.mdc.mspring.app.config;

import com.mdc.mspring.app.bean.TestImportBean;
import com.mdc.mspring.context.anno.Bean;
import com.mdc.mspring.context.anno.Configuration;
import com.mdc.mspring.context.anno.Import;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/17/8:48
 * @Description:
 */
@Configuration
public class TestImportConfig {
    @Bean
    public TestImportBean testImportBean() {
        return new TestImportBean();
    }
}
