package com.mdc.mspring.boot.webapp.config;

import com.mdc.mspring.context.anno.ComponentScan;
import com.mdc.mspring.context.anno.Configuration;
import com.mdc.mspring.context.anno.Import;
import com.mdc.mspring.mvc.config.WebMvcConfiguration;

@Configuration
@Import({ WebMvcConfiguration.class })
@ComponentScan("com.mdc.mspring.boot.webapp")
public class AppConfig {

}
