package com.mdc.mspring.context.utils;

import com.mdc.mspring.context.annotation.ComponentScan;
import com.mdc.mspring.context.annotation.Order;
import com.mdc.mspring.context.annotation.Primary;
import com.mdc.mspring.context.exception.BeanDefinitionException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/11/15:52
 * @Description:
 */
public class ClassUtils {
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<? extends T> annoClass) {
        return (T) getAnnotation(clazz, annoClass, new HashSet<>());
    }

    /**
     * Recursively finds if the class(clazz) has been specified for target
     * annotation(annoClass)
     *
     * @param clazz,    annoClass
     * @param annoClass
     * @return
     */
    @SuppressWarnings("unchecked")
    private static Annotation getAnnotation(Class<?> clazz, Class<? extends Annotation> annoClass,
                                            Set<Class<Annotation>> annotationSet) {
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
            throw new BeanDefinitionException(
                    "More than one public constructor found in class " + clazz.getName() + ".");
        }
        return cons[0];
    }

    public static Annotation[] getTargetAnnotaionOnConstructorArgs(Constructor<?> constructor,
                                                                   Set<Class<? extends Annotation>> annotationClassesSet) {
        var annotations = constructor.getParameterAnnotations();
        Annotation[] result = new Annotation[annotations.length];
        getTargetAnnotation(annotations, result, annotationClassesSet);
        return result;
    }

    public static Annotation[] getTargetAnnotaionOnMethodArgs(Method method,
                                                              Set<Class<? extends Annotation>> annotationClassesSet) {
        var annotations = method.getParameterAnnotations();
        Annotation[] result = new Annotation[annotations.length];
        getTargetAnnotation(annotations, result, annotationClassesSet);
        return result;
    }

    private static void getTargetAnnotation(Annotation[][] annotations, Annotation[] target,
                                            Set<Class<? extends Annotation>> annotationClassesSet) {
        for (int i = 0; i < annotations.length; i++) {
            for (Annotation annotation : annotations[i]) {
                if (annotationClassesSet.contains(annotation.annotationType())) {
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

    public static int getOrder(Executable executable) {
        Order order = ((Order) executable.getAnnotation(Order.class));
        return order != null ? order.value() : 0;
    }

    public static boolean getPrimary(Class<?> clazz) {
        Primary primary = (Primary) clazz.getAnnotation(Primary.class);
        return primary != null && primary.value();
    }

    public static boolean getPrimary(Executable executable) {
        Primary primary = ((Primary) executable.getAnnotation(Primary.class));
        return primary != null && primary.value();
    }

    public static boolean isAnnotation(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return clazz.isAnnotation();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T> T callMethodOf(Object object, String method, Class<T> returnType, Object... args)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method targetMethod = null;
        try {
            targetMethod = object.getClass().getMethod(method,
                    Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        if (targetMethod == null)
            return null;
        targetMethod.setAccessible(true);
        return (T) targetMethod.invoke(object, args);
    }

    public static String getBasePackage(Class<?> clazz) {
        if (clazz.getAnnotation(ComponentScan.class) != null) {
            String basePackage = null;
            if (StringUtils.isEmpty(basePackage = clazz.getAnnotation(ComponentScan.class).value())) {
                basePackage = clazz.getPackage().getName();
            }
            return basePackage;
        } else {
            return clazz.getPackage().getName();
        }
    }
}
