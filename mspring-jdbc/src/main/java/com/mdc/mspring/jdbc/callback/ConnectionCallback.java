package com.mdc.mspring.jdbc.callback;

import com.mdc.mspring.context.annotation.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/14/22:02
 * @Description:
 */
@FunctionalInterface
public interface ConnectionCallback<T> {
    @Nullable
    T doInConnection(Connection connection) throws SQLException;
}
