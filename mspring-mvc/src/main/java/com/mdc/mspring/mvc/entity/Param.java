package com.mdc.mspring.mvc.entity;

import com.mdc.mspring.context.entity.ioc.BeanDefinition;
import com.mdc.mspring.mvc.anno.PathVariable;
import com.mdc.mspring.mvc.anno.RequestBody;
import com.mdc.mspring.mvc.anno.RequestParam;
import com.mdc.mspring.mvc.enums.ParamType;
import com.mdc.mspring.utils.ClassUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.ServerException;
import java.util.Arrays;
import java.util.Set;

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

    /*
     * 解析方法参数并封装为Param[]
     */
    public static Param[] parse(Method method, final BeanDefinition definition) {
        final Set<?> targetAnnotationSet = Set.of(
                RequestParam.class,
                RequestBody.class,
                PathVariable.class);
        // 将method的参数列表转换为
        return (Param[]) Arrays.stream(method.getParameters()).map(
                param -> {
                    // 1 获取各个参数的注解
                    Annotation annotation = null;
                    try {
                        annotation = Arrays.stream(param.getAnnotations()).filter(
                                anno -> targetAnnotationSet.contains(anno.annotationType())).findFirst()
                                .orElseThrow(() -> new ServerException("参数注解错误"));
                    } catch (ServerException e) {
                    }
                    String name = param.getName();
                    ParamType paramType = null;
                    if (annotation == null) {
                        paramType = ParamType.REQUEST_PARAM; // default value
                    } else if (annotation.getClass() == RequestParam.class) {
                        paramType = ParamType.REQUEST_PARAM;
                    } else if (annotation.getClass() == PathVariable.class) {
                        paramType = ParamType.PATH_VARIABLE;
                    } else if (annotation.getClass() == RequestBody.class) {
                        paramType = ParamType.REQUEST_BODY;
                    }
                    String defaultValue = "";
                    try {
                        defaultValue = ClassUtils.callMethodOf(annotation, "defaultValue", String.class);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return Param.builder().name(name).paramType(paramType).classType(param.getType())
                            .defaultValue(defaultValue).build();
                }).toArray();
    }
}
