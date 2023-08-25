package com.mdc.mspring.mvc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/21/17:13
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
public class Dispatcher {
    // 是否返回REST:
    boolean isRest;
    // 是否有@ResponseBody:
    boolean isResponseBody;
    // 是否返回void:
    boolean isVoid;
    // 正则匹配的{}字符串形式
    String urlPatternStr;
    // URL正则匹配:
    Pattern urlPattern;
    // Bean实例:
    Object controller;
    // 处理方法:
    Method handlerMethod;
    // 方法参数:
    Param[] methodParameters;
}
