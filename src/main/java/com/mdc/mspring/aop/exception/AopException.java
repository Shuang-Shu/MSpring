package com.mdc.mspring.aop.exception;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/16/11:12
 * @Description:
 */
public class AopException extends RuntimeException {
    public AopException(String message) {
        super(message);
    }

    public AopException(String message, Throwable cause) {
        super(message, cause);
    }
}
