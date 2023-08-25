package com.mdc.mspring.context.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/15/14:17
 * @Description:
 */
public class MapperUtils {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }

    @SuppressWarnings("unchecked")
    public static <T> T constructAndSetFieldsUseResultSet(Class<T> clazz, ResultSet resultSet)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, SQLException {
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.stream(clazz.getDeclaredFields()).toList());
        fields.addAll(Arrays.stream(clazz.getFields()).toList());
        T obj = (T) clazz.getConstructors()[0].newInstance();
        for (Field field : fields) {
            field.setAccessible(true);
            Object fieldObj;
            try {
                fieldObj = resultSet.getObject(field.getName());
            } catch (SQLException e) {
                continue;
            }
            field.set(obj, fieldObj);
        }
        return obj;
    }
}
