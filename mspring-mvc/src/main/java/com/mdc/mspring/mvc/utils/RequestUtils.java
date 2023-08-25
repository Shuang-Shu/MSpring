package com.mdc.mspring.mvc.utils;

import com.mdc.mspring.mvc.entity.Param;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtils {
    public static Object[] parseParam(HttpServletRequest httpRequest, Param[] params) {
        Object[] result = null;
        // 获取请求头、请求体、和URL中存在的参数
        // if (paramType == ParamType.REQUEST_PARAM) {
        //     var header=httpRequest.getHeader(null)
        // } else if (paramType == ParamType.REQUEST_BODY) {

        // } else if (paramType == ParamType.PATH_VARIABLE) {

        // } else if (paramType == ParamType.SERVLET_VARIABLE) {

        // }
        return result;
    }
}
