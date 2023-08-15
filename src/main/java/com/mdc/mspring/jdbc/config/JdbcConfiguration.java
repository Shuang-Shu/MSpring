package com.mdc.mspring.jdbc.config;

import com.mdc.mspring.context.anno.Autowired;
import com.mdc.mspring.context.anno.Bean;
import com.mdc.mspring.context.anno.Configuration;
import com.mdc.mspring.context.anno.Value;
import com.mdc.mspring.jdbc.template.JdbcTemplate;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/14/16:59
 * @Description:
 */
@Configuration
public class JdbcConfiguration {
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
        return new HikariDataSource(config);
    }

    @Bean(value = "jdbcTemplate")
    JdbcTemplate jdbcTemplate(@Autowired DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
