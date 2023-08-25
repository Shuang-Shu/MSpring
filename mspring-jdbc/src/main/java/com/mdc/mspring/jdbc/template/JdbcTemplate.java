package com.mdc.mspring.jdbc.template;

import com.mdc.mspring.context.utils.MapperUtils;
import com.mdc.mspring.jdbc.RowMapper;
import com.mdc.mspring.jdbc.callback.ConnectionCallback;
import com.mdc.mspring.jdbc.callback.PreparedStatementCallback;
import com.mdc.mspring.jdbc.callback.PreparedStatementCreator;
import com.mdc.mspring.jdbc.exception.DataAccessException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/14/21:59
 * @Description:
 */
public class JdbcTemplate {
    final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(ConnectionCallback<T> action) {
        try (Connection newConnection = dataSource.getConnection()) {
            T result = action.doInConnection(newConnection);
            return result;
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public <T> T execute(PreparedStatementCreator creator, PreparedStatementCallback<T> action) {
        return execute(
                con -> {
                    try (PreparedStatement ps = creator.createPreparedStatement(con)) {
                        return action.doInPreparedStatement(ps);
                    }
                });
    }

    public int update(String sql, Object... args) {
        return execute(
                getPreparedStatementCreator(sql, args),
                (PreparedStatement ps) -> {
                    return ps.executeUpdate();
                });
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(
                getPreparedStatementCreator(sql, args),
                (PreparedStatement ps) -> {
                    List<T> result = new ArrayList<>();
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            result.add(rowMapper.mapRow(rs, rs.getRow()));
                        }
                        return result;
                    }
                });
    }

    public <T> List<T> queryForList(String sql, Class<T> clazz, Object... args) {
        return execute(
                getPreparedStatementCreator(sql, args),
                (PreparedStatement ps) -> {
                    List<T> result = new ArrayList<>();
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            try {
                                result.add(MapperUtils.constructAndSetFieldsUseResultSet(clazz, rs));
                            } catch (Exception e) {
                                throw new DataAccessException(e.getMessage());
                            }
                        }
                        return result;
                    }
                });
    }

    public Number queryForNumber(String sql, Object... args) {
        return queryForObject(sql, NumberRowMapper.instance, args);
    }

    public <T> T queryForObject(String sql, Class<T> clazz, Object... args) {
        return execute(
                getPreparedStatementCreator(sql, args),
                (PreparedStatement ps) -> {
                    T result = null;
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            try {
                                result = MapperUtils.constructAndSetFieldsUseResultSet(clazz, rs);
                            } catch (Exception e) {
                                throw new DataAccessException(e.getMessage());
                            }
                        }
                        return result;
                    }
                });
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(
                getPreparedStatementCreator(sql, args),
                (PreparedStatement ps) -> {
                    T result = null;
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            result = rowMapper.mapRow(rs, rs.getRow());
                        }
                        return result;
                    }
                });
    }

    public Number updateAndReturnGeneratedKey(String sql, Object... args) {
        return execute(
                // PreparedStatementCreator
                (Connection con) -> {
                    var ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    bindArgs(ps, args);
                    return ps;
                },
                // PreparedStatementCallback
                (PreparedStatement ps) -> {
                    int n = ps.executeUpdate();
                    if (n == 0) {
                        throw new DataAccessException("0 rows inserted.");
                    }
                    if (n > 1) {
                        throw new DataAccessException("Multiple rows inserted.");
                    }
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        while (keys.next()) {
                            return (Number) keys.getObject(1);
                        }
                    }
                    throw new DataAccessException("Should not reach here.");
                });

    }

    public PreparedStatementCreator preparedStatementCreator(String sql, Object... args) {
        return null;
    }

    public void bindArgs(PreparedStatement ps, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }

    private PreparedStatementCreator getPreparedStatementCreator(String sql, Object... args) {
        return (Connection con) -> {
            PreparedStatement ps = con.prepareStatement(sql);
            bindArgs(ps, args);
            return ps;
        };
    }
}

class StringRowMapper implements RowMapper<String> {

    static StringRowMapper instance = new StringRowMapper();

    @Override
    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString(1);
    }
}

class BooleanRowMapper implements RowMapper<Boolean> {

    static BooleanRowMapper instance = new BooleanRowMapper();

    @Override
    public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getBoolean(1);
    }
}

class NumberRowMapper implements RowMapper<Number> {

    static NumberRowMapper instance = new NumberRowMapper();

    @Override
    public Number mapRow(ResultSet rs, int rowNum) throws SQLException {
        return (Number) rs.getObject(1);
    }
}