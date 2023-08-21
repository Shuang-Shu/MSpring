package com.mdc.mspring.mvc.entity;

import com.mdc.mspring.mvc.enums.ParamType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/21/17:13
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
public class Param {
    // 参数名称:
    String name;
    // 参数类型:
    ParamType paramType;
    // 参数Class类型:
    Class<?> classType;
    // 参数默认值
    String defaultValue;

    public static Param[] parse(Method method) {
        throw new UnsupportedOperationException();
    }
}
