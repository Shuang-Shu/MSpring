package com.mdc.mspring.utils;

import com.mdc.mspring.anno.ioc.Order;
import com.mdc.mspring.anno.ioc.PostConstruct;
import com.mdc.mspring.anno.ioc.Primary;
import com.mdc.mspring.exception.BeanDefinitionException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/11/15:52
 * @Description:
 */
public class ClassUtils {
    /**
     * Recursively finds if the class(clazz) has been specified for target annotation(annoClass)
     *
     * @param clazz,    annoClass
     * @param annoClass
     * @return
     */
    public static Annotation getAnnotation(Class<?> clazz, Class<? extends Annotation> annoClass, Set<Class<Annotation>> annotationSet) {
        if (annotationSet.contains(clazz)) {
            return null;
        } else {
            annotationSet.add((Class<Annotation>) clazz);
        }
        Annotation[] annotations = clazz.getAnnotationsByType(annoClass);
        Annotation result = null;
        if (annotations.length > 0) {
            return annotations[0];
        } else {
            Annotation[] allAnnotations = clazz.getAnnotations();
            for (Annotation annotation : allAnnotations) {
                if ((result = getAnnotation(annotation.annotationType(), annoClass, annotationSet)) != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public static Method getAnnotationMethod(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getAnnotation(annotationClass) != null) {
                return method;
            }
        }
        return null;
    }

    public static Constructor<?> getSuitableConstructor(Class<?> clazz) throws NoSuchMethodException {
        Constructor<?>[] cons = clazz.getConstructors();
        if (cons.length == 0) {
            cons = clazz.getDeclaredConstructors();
            if (cons.length != 1) {
                throw new BeanDefinitionException("More than one constructor found in class " + clazz.getName() + ".");
            }
        }
        if (cons.length != 1) {
            throw new BeanDefinitionException("More than one public constructor found in class " + clazz.getName() + ".");
        }
        return cons[0];
    }

    public static Annotation[] getTargetAnnotaionOnConstructorArgs(Constructor<?> constructor, Class<? extends Annotation> annotationClass) {
        var annotations = constructor.getParameterAnnotations();
        Annotation[] result = new Annotation[annotations.length];
        getTargetAnnotation(annotations, result, annotationClass);
        return result;
    }

    public static Annotation[] getTargetAnnotaionOnMethodArgs(Method method, Class<? extends Annotation> annotationClass) {
        var annotations = method.getParameterAnnotations();
        Annotation[] result = new Annotation[annotations.length];
        getTargetAnnotation(annotations, result, annotationClass);
        return result;
    }

    private static void getTargetAnnotation(Annotation[][] annotations, Annotation[] target, Class<? extends Annotation> annotationClass) {
        for (int i = 0; i < annotations.length; i++) {
            for (Annotation annotation : annotations[i]) {
                if (annotation.annotationType() == annotationClass) {
                    target[i] = annotation;
                    break;
                }
            }
        }
    }

    public static int getOrder(Class<?> clazz) {
        Order order = (Order) clazz.getAnnotation(Order.class);
        return order != null ? order.value() : 0;
    }

    public static int getOrderMethod(Method method) {
        Order order = (Order) method.getAnnotation(Order.class);
        return order != null ? order.value() : 0;
    }

    public static boolean getPrimary(Class<?> clazz) {
        Primary primary = ((Primary) clazz.getAnnotation(Primary.class));
        return primary != null && primary.value();
    }

    public static boolean getPrimaryMethod(Method method) {
        Primary primary = ((Primary) method.getAnnotation(Primary.class));
        return primary != null && primary.value();
    }
}
