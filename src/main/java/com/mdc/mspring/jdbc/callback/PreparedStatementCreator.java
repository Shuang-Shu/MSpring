package com.mdc.mspring.jdbc.callback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/15/10:32
 * @Description:
 */
@FunctionalInterface
public interface PreparedStatementCreator {
    PreparedStatement createPreparedStatement(Connection con) throws SQLException;
}
