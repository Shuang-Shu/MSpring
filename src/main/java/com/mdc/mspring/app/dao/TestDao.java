package com.mdc.mspring.app.dao;

import com.mdc.mspring.anno.ioc.Autowired;
import com.mdc.mspring.anno.ioc.Component;
import com.mdc.mspring.anno.ioc.Value;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/12/18:14
 * @Description:
 */
@Component
public class TestDao {
    public int age;
    @Value("username")
    public String username;
    @Value("password")
    public String passwd;
}
