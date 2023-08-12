package com.mdc.mspring.exception;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/11/15:50
 * @Description:
 */
public class DuplicatedBeanNameException extends RuntimeException {
    public DuplicatedBeanNameException(String msg) {
        super(msg);
    }
}
