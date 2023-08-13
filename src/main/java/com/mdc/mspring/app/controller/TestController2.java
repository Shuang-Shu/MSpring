package com.mdc.mspring.app.controller;

import com.mdc.mspring.app.service.TestService;
import com.mdc.mspring.context.anno.Autowired;
import com.mdc.mspring.context.anno.Component;
import com.mdc.mspring.context.anno.Controller;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/13/17:39
 * @Description:
 */
@Controller
public class TestController2 {
    @Autowired
    private TestService service;
}
