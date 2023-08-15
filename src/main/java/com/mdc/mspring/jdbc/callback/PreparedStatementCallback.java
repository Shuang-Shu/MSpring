package com.mdc.mspring.jdbc.callback;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/15/10:30
 * @Description:
 */
@FunctionalInterface
public interface PreparedStatementCallback<T> {
    T doInPreparedStatement(PreparedStatement ps) throws SQLException;
}
