package com.mdc.mspring.webapp.controller;

import java.util.Map;

import com.mdc.mspring.mvc.anno.Controller;
import com.mdc.mspring.mvc.anno.GetMapping;
import com.mdc.mspring.mvc.anno.PathVariable;
import com.mdc.mspring.mvc.anno.RequestParam;
import com.mdc.mspring.mvc.anno.ResponseBody;
import com.mdc.mspring.mvc.entity.ModelAndView;

@Controller
public class BasicController {
    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        return "hello";
    }

    @GetMapping("/product/{id}")
    ModelAndView product(@PathVariable("id") long id, @RequestParam("name") String name) {
        return new ModelAndView("/product.html",
                Map.of("name", name, "product", Map.of("id", id, "name", "MDC Software")));
    }
}
