package com.mdc.mspring.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/15/11:51
 * @Description:
 */
@FunctionalInterface
public interface RowMapper<T> {
    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
