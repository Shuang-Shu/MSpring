package com.mdc.mspring.jdbc.tx;

import java.sql.Connection;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/16/9:06
 * @Description:
 */
public class TransactionStatus {
    final Connection connection;

    public TransactionStatus(Connection connection) {
        this.connection = connection;
    }

    public void remove() {
    }
}
