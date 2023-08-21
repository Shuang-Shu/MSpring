package com.mdc.mspring.mvc.config;

import com.mdc.mspring.context.anno.*;
import com.mdc.mspring.mvc.view.ViewResolver;
import com.mdc.mspring.mvc.view.impl.FreeMarkerViewResolver;

import javax.servlet.ServletContext;
import java.util.Objects;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/21/17:33
 * @Description:
 */
@Configuration
@ComponentScan("com.mdc.mspring.mvc")
public class WebMvcConfiguration {
    private static ServletContext servletContext = null;

    static void setServletContext(ServletContext servletContext) {
        WebMvcConfiguration.servletContext = servletContext;
    }

    @Bean
    ViewResolver viewResolver(//
                              @Autowired ServletContext servletContext, //
                              @Value("${summer.web.freemarker.template-path:/WEB-INF/templates}") String templatePath, //
                              @Value("${summer.web.freemarker.template-encoding:UTF-8}") String templateEncoding) {
        return new FreeMarkerViewResolver(servletContext, templatePath, templateEncoding);
    }

    @Bean
    ServletContext servletContext() {
        return Objects.requireNonNull(servletContext, "ServletContext is not set.");
    }
}
