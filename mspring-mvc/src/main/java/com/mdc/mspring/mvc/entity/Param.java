package com.mdc.mspring.mvc.entity;

import com.mdc.mspring.context.utils.ClassUtils;
import com.mdc.mspring.mvc.anno.PathVariable;
import com.mdc.mspring.mvc.anno.RequestBody;
import com.mdc.mspring.mvc.anno.RequestParam;
import com.mdc.mspring.mvc.enums.ParamType;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.ServerException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

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
    // 参数是否必须F
    boolean required;

    public final static Set<?> targetAnnotationSet = Set.of(
            RequestParam.class,
            RequestBody.class,
            PathVariable.class);

    public final static Set<?> servletVariableTypeSet = Set.of(
            HttpServletRequest.class,
            HttpServletResponse.class,
            HttpSession.class,
            ServletContext.class
    );

    /*
     * 解析方法参数并封装为Param[]
     */
    public static Param[] parse(Method method) {
        // 将method的参数列表转换为
        return Arrays.stream(method.getParameters()).map(
                param -> {
                    // 1 获取各个参数的注解
                    Annotation annotation = null;
                    try {
                        annotation = Arrays.stream(param.getAnnotations()).filter(
                                        anno -> targetAnnotationSet.contains(anno.annotationType())).findFirst()
                                .orElseThrow(() -> new ServerException("参数注解错误"));
                    } catch (ServerException e) {
                    }
                    ParamType paramType = null;
                    if (annotation == null) {
                        paramType = ParamType.REQUEST_PARAM; // default value
                    } else if (annotation instanceof RequestParam) {
                        paramType = ParamType.REQUEST_PARAM;
                    } else if (annotation instanceof PathVariable) {
                        paramType = ParamType.PATH_VARIABLE;
                    } else if (annotation instanceof RequestBody) {
                        paramType = ParamType.REQUEST_BODY;
                    }
                    if (annotation != null) {
                        // 获取参数名（针对非SERVLET_VARIABLE类型的参数）
                        String defaultValue = "";
                        String name = "";
                        boolean required = false;
                        try {
                            required = Boolean.TRUE.equals(ClassUtils.callMethodOf(annotation, "required", Boolean.class));
                            name = ClassUtils.callMethodOf(annotation, "value", String.class);
                            defaultValue = ClassUtils.callMethodOf(annotation, "defaultValue", String.class);
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        return Param.builder().name(name).paramType(paramType).classType(param.getType()).required(required)
                                .defaultValue(defaultValue).build();
                    } else if (servletVariableTypeSet.contains(param.getType())) {
                        // 获取参数名（针对SERVLET_VARIABLE类型的参数）
                        return Param.builder().name(param.getName()).paramType(ParamType.SERVLET_VARIABLE).classType(param.getType())
                                .defaultValue("").build();
                    } else {
                        try {
                            throw new ServletException("Param Annotation Error");
                        } catch (ServletException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).collect(Collectors.toList()).toArray(new Param[method.getParameters().length]);
    }
}
