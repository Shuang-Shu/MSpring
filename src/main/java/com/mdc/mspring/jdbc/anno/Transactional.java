package com.mdc.mspring.jdbc.anno;

import com.mdc.mspring.aop.anno.Around;

import java.lang.annotation.*;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/16/8:54
 * @Description:
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Transactional {
    String value() default "platformTransactionManager";
}
