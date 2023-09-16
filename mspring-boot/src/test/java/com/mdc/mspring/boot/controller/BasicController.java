package com.mdc.mspring.boot.controller;

import com.mdc.mspring.mvc.annotation.Controller;
import com.mdc.mspring.mvc.annotation.GetMapping;
import com.mdc.mspring.mvc.annotation.PathVariable;
import com.mdc.mspring.mvc.annotation.ResponseBody;

@Controller
public class BasicController {
    @GetMapping("/hello/{id}")
    @ResponseBody
    public String hello(@PathVariable("id") String id) {
        return "<h1>hello<//h1>";
    }
}
