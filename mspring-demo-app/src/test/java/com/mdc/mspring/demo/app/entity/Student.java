package com.mdc.mspring.demo.app.entity;

import com.mdc.mspring.context.annotation.Autowired;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Student {
    @Autowired
    private Teacher teacher;

    private String name;
    private Integer age;
}
