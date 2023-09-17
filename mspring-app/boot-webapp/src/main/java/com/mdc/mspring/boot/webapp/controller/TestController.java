package com.mdc.mspring.boot.webapp.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import com.mdc.mspring.mvc.annotation.Controller;
import com.mdc.mspring.mvc.annotation.GetMapping;
import com.mdc.mspring.mvc.annotation.PathVariable;
import com.mdc.mspring.mvc.annotation.ResponseBody;
import com.mdc.mspring.mvc.entity.ModelAndView;

@Controller
public class TestController {
    @ResponseBody
    @GetMapping("/hello/{id}")
    public String hello(@PathVariable("id") String id) {
        return "hello! " + id;
    }

    @GetMapping("/name/{name}")
    public ModelAndView myName(@PathVariable("name") String name) throws UnsupportedEncodingException {
        name = URLDecoder.decode(name, "UTF-8");
        return new ModelAndView("/hello.html", Map.of("name", name));
    }
}
