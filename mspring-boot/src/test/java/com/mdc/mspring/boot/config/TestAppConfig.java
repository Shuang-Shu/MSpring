package com.mdc.mspring.boot.config;

import com.mdc.mspring.context.annotation.ComponentScan;
import com.mdc.mspring.context.annotation.Configuration;
import com.mdc.mspring.context.annotation.Import;
import com.mdc.mspring.mvc.config.WebMvcConfiguration;

@Configuration
@Import({WebMvcConfiguration.class})
@ComponentScan("com.mdc.mspring.boot")
public class TestAppConfig {
}
