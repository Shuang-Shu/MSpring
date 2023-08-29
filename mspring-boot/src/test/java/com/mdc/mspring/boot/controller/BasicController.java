package com.mdc.mspring.boot.controller;

import com.mdc.mspring.mvc.anno.Controller;
import com.mdc.mspring.mvc.anno.GetMapping;
import com.mdc.mspring.mvc.anno.PathVariable;
import com.mdc.mspring.mvc.anno.ResponseBody;

@Controller
public class BasicController {
    @GetMapping("/hello/{id}")
    @ResponseBody
    public String hello(@PathVariable("id") String id) {
        return "<h1>hello<//h1>";
    }
}
