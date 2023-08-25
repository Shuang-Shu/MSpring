package com.mdc.mspring.mvc.utils;

public class StringUtils {
    public static Object parseStr(String str, Class<?> clazz) {
        if (clazz == String.class) {
            return str;
        } else if (clazz == Integer.class || clazz == int.class) {
            return Integer.parseInt(str);
        } else if (clazz == Long.class || clazz == long.class) {
            return Long.parseLong(str);
        } else if (clazz == Double.class || clazz == double.class) {
            return Double.parseDouble(str);
        } else if (clazz == Float.class || clazz == float.class) {
            return Float.parseFloat(str);
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return Boolean.parseBoolean(str);
        } else if (clazz == Byte.class || clazz == byte.class) {
            return Byte.parseByte(str);
        } else if (clazz == Short.class || clazz == short.class) {
            return Short.parseShort(str);
        } else if (clazz == Character.class || clazz == char.class) {
            return str.charAt(0);
        } else {
            return null;
        }
    }
}
