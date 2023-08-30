package com.mdc.mspring.app.service;

import com.mdc.mspring.app.dao.TestDao;
import com.mdc.mspring.app.dao.TestDao2;
import com.mdc.mspring.app.dao.TestDaoProxy2;
import com.mdc.mspring.context.anno.Autowired;
import com.mdc.mspring.context.anno.Component;
import com.mdc.mspring.context.anno.Service;
import com.mdc.mspring.context.anno.Value;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/12/18:15
 * @Description:
 */
@Service
public class TestService {
    @Autowired
    private TestDao dao;

    @Autowired
    private TestDao2 daoProxy2;

    @Value("proxy.test")
    private String name;

    public void service() {
        System.out.println("service");
    }
}
