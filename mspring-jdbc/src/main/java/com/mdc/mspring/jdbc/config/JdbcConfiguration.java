package com.mdc.mspring.jdbc.config;

import com.mdc.mspring.aop.config.AopConfiguration;
import com.mdc.mspring.context.anno.*;
import com.mdc.mspring.jdbc.processor.TransactionalBeanPostProcessor;
import com.mdc.mspring.jdbc.template.JdbcTemplate;
import com.mdc.mspring.jdbc.tx.PlatformTransactionManager;
import com.mdc.mspring.jdbc.tx.impl.DataSourceTransactionManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/14/16:59
 * @Description:
 */
@Configuration
@Import(AopConfiguration.class)
@ComponentScan("com.mdc.mspring.jdbc")
public class JdbcConfiguration {
    private final Logger logger = LoggerFactory.getLogger(JdbcConfiguration.class);

    @Bean(value = "dataSource", destroyMethod = "close")
    DataSource dataSource(
            @Value("${mspring.datasource.url}") String url,
            @Value("${mspring.datasource.username}") String username,
            @Value("${mspring.datasource.password}") String password,
            @Value("${mspring.datasource.driver-class-name:}") String driver,
            @Value("${mspring.datasource.maximum-pool-size:20}") int maximumPoolSize,
            @Value("${mspring.datasource.minimum-pool-size:1}") int minimumPoolSize,
            @Value("${mspring.datasource.connection-timeout:30000}") int connTimeout
    ) {
        var config = new HikariConfig();
        config.setAutoCommit(false);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        if (driver != null) {
            config.setDriverClassName(driver);
        }
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumPoolSize);
        config.setConnectionTimeout(connTimeout);
        logger.info("Loading datasource config: url={}, username={}, password={}, driver={}, maximumPoolSize={}, minimumPoolSize={}, connTimeout={}",
                url, username, password, driver, maximumPoolSize, minimumPoolSize, connTimeout);
        return new HikariDataSource(config);
    }

    @Bean(value = "jdbcTemplate")
    JdbcTemplate jdbcTemplate(@Autowired DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    TransactionalBeanPostProcessor transactionBeanPostProcessor() {
        return new TransactionalBeanPostProcessor();
    }

    @Bean
    PlatformTransactionManager platformTransactionManager(@Autowired DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
