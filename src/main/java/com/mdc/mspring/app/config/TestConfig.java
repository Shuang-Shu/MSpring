package com.mdc.mspring.app.config;

import com.mdc.mspring.app.bean.TestBean;
import com.mdc.mspring.app.service.TestService;
import com.mdc.mspring.context.anno.*;
import com.mdc.mspring.jdbc.config.JdbcConfiguration;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/11/17:24
 * @Description:
 */
@Import(JdbcConfiguration.class)
@Configuration
@ComponentScan("com.mdc.mspring")
public class TestConfig {
    @Order(10)
    @Primary(value = true)
    @Bean(value = "testBeanName", initMethod = "testBeanInit", destroyMethod = "testBeanDestroy")
    public TestBean factory(@Autowired TestService testService) {
        return new TestBean();
    }
}
