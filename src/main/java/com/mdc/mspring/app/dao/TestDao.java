package com.mdc.mspring.app.dao;

import com.mdc.mspring.context.anno.Component;
import com.mdc.mspring.context.anno.Value;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/12/18:14
 * @Description:
 */
@Component
public class TestDao {
    @Value("username")
    public String username;
    @Value("password")
    public String passwd;

    public void test() {
        System.out.println("dao 2");
    }
}