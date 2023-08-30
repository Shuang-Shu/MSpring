package com.mdc.mspring.app.dao;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/13/20:07
 * @Description:
 */
public class TestDaoProxy2 extends TestDao2 {
    private TestDao2 testDao;

    public TestDaoProxy2(TestDao2 testDao) {
        this.testDao = testDao;
    }

    @Override
    public void test() {
        System.out.println("proxy before 2");
        testDao.test();
        System.out.println("proxy after 2");
    }
}
