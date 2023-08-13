package com.mdc.mspring.app.dao;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/13/19:49
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TestDaoProxy extends TestDao {
    private TestDao testDao;

    public TestDaoProxy(TestDao testDao) {
        this.testDao = testDao;
    }

    @Override
    public void test() {
        System.out.println("proxy before");
        testDao.test();
        System.out.println("proxy after");
    }
}
