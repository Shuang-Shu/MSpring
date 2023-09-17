package com.mdc.mspring.webapp.config;

import com.mdc.mspring.context.annotation.ComponentScan;
import com.mdc.mspring.context.annotation.Configuration;
import com.mdc.mspring.context.annotation.Import;
import com.mdc.mspring.jdbc.config.JdbcConfiguration;
import com.mdc.mspring.mvc.config.WebMvcConfiguration;

@Configuration
@Import({WebMvcConfiguration.class, JdbcConfiguration.class})
@ComponentScan("com.mdc.mspring.webapp")
public class WebAppConfig {

}
