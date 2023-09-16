package com.mdc.mspring.mvc.test;

import com.mdc.mspring.context.annotation.ComponentScan;
import com.mdc.mspring.context.annotation.Configuration;
import com.mdc.mspring.context.annotation.Import;
import com.mdc.mspring.mvc.config.WebMvcConfiguration;

@Configuration
@ComponentScan("com.mdc.mspring.mvc.test")
@Import(WebMvcConfiguration.class)
public class ControllerConfiguration {

}
