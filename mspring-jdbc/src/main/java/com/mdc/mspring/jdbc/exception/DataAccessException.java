package com.mdc.mspring.jdbc.exception;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/14/22:01
 * @Description:
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message) {
        super(message);
    }
}
