package com.mdc.mspring.mvc.enums;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/21/17:12
 * @Description:
 */
public enum ParamType {
    PATH_VARIABLE(0), REQUEST_PARAM(1), REQUEST_BODY(2), SERVLET_VARIABLE(3);
    private int code;

    ParamType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
