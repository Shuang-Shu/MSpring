package com.mdc.mspring.exception;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/11/15:46
 * @Description:
 */
public class NoUniqueBeanDefinitionException extends RuntimeException {
    public NoUniqueBeanDefinitionException(String msg) {
        super(msg);
    }
}
