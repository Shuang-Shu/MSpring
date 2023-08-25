package com.mdc.mspring.demo.app.config;

import com.mdc.mspring.context.anno.Bean;
import com.mdc.mspring.context.anno.ComponentScan;
import com.mdc.mspring.context.anno.Configuration;
import com.mdc.mspring.demo.app.entity.Student;
import com.mdc.mspring.demo.app.entity.Teacher;

@Configuration
@ComponentScan("com.mdc.mspring.demo.app")
public class TestConfig {
    @Bean
    public Student initStudent() {
        return Student.builder().name("mdc").age(18).build();
    }

    @Bean
    public Teacher initTeacher() {
        return Teacher.builder().name("mdc-teacher").age(38).build();
    }
}
