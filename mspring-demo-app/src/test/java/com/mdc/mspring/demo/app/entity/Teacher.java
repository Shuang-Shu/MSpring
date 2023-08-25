package com.mdc.mspring.demo.app.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class Teacher {
    private String name;
    private Integer age;
}
