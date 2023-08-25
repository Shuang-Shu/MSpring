package com.mdc.mspring.mvc.controller;

import com.mdc.mspring.mvc.anno.Controller;
import com.mdc.mspring.mvc.anno.GetMapping;
import com.mdc.mspring.mvc.anno.PathVariable;

@Controller
public class TestController {
    @GetMapping("/hello/{name}")
    public String hello(@PathVariable("name") String name) {
        return "hello: " + name;
    }
}
