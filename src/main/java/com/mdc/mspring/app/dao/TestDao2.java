package com.mdc.mspring.app.dao;

import com.mdc.mspring.context.anno.Component;
import com.mdc.mspring.context.anno.Value;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/13/20:06
 * @Description:
 */
@Component
public class TestDao2 {
    @Value("username")
    public String username;
    @Value("password")
    public String passwd;

    public void test() {
        System.out.println("dao");
    }
}
