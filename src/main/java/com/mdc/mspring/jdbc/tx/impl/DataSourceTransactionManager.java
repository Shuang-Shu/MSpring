package com.mdc.mspring.jdbc.tx.impl;

import com.mdc.mspring.jdbc.exception.TransactionException;
import com.mdc.mspring.jdbc.tx.PlatformTransactionManager;
import com.mdc.mspring.jdbc.tx.TransactionStatus;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/16/9:05
 * @Description:
 */
public class DataSourceTransactionManager implements PlatformTransactionManager, InvocationHandler {
    final static ThreadLocal<TransactionStatus> transactionStatusThreadLocal = new ThreadLocal<>();
    final DataSource dataSource;

    public DataSourceTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        TransactionStatus transactionStatus = transactionStatusThreadLocal.get();
        if (transactionStatus == null) {
            // transaction is not opened
            try (Connection connection = dataSource.getConnection()) { // 用于自动释放资源
                boolean autoCommit = connection.getAutoCommit();
                System.out.println("transaction start");
                if (autoCommit) {
                    connection.setAutoCommit(false);
                }
                try {
                    transactionStatusThreadLocal.set(new TransactionStatus(connection));
                    Object r = method.invoke(o, objects);
                    connection.commit();
                    System.out.println("transaction commit");
                    return r;
                } catch (InvocationTargetException e) {
                    // 回滚事务:
                    TransactionException te = new TransactionException(e.getCause());
                    try {
                        connection.rollback();
                    } catch (SQLException sqle) {
                        te.addSuppressed(sqle);
                    }
                    throw te;
                } finally {
                    // 删除ThreadLocal状态:
                    transactionStatusThreadLocal.remove();
                    if (autoCommit) {
                        connection.setAutoCommit(true);
                    }
                }
            }
        } else {
            return method.invoke(o, objects);
        }
    }
}