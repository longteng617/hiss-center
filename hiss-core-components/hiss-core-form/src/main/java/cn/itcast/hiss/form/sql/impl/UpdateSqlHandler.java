package cn.itcast.hiss.form.sql.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.itcast.hiss.form.annocation.FieldName;
import cn.itcast.hiss.form.annocation.TableName;
import cn.itcast.hiss.form.pojo.BasePojo;
import cn.itcast.hiss.form.sql.enums.FunctionEnum;
import cn.itcast.hiss.form.sql.manage.HissSqlExchangeHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * UpdateSqlHandler
 *
 * @author: wgl
 * @describe: TODO
 * @date: 2022/12/28 10:10
 */
@Component
public class UpdateSqlHandler implements HissSqlExchangeHandler {
    @Override
    public String exchangeSql(BasePojo... objects) {
        String builder = "UPDATE ";
        StringBuilder stringBuilder = new StringBuilder();
        if (objects != null && objects.length > 0) {
            BasePojo object = objects[0];
            Class<?> clazz = object.getClass();

            stringBuilder.append("update ");
            stringBuilder.append(getTableName(clazz));
            stringBuilder.append("set ");

            StringJoiner stringJoiner = new StringJoiner(",");
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object value = field.get(object);
                    if (value != null) {
                        if (field.isAnnotationPresent(FieldName.class)) {
                            // 字段有自定义注解
                            FieldName fieldNameAnnotation = field.getAnnotation(FieldName.class);
                            String columnName = fieldNameAnnotation.value();
                            stringJoiner.add(columnName + " = " + formatValue(value));
                        } else {
                            // 字段没有自定义注解，直接使用字段名
                            stringJoiner.add(field.getName() + " = " + formatValue(value));
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            stringBuilder.append(stringJoiner.toString());
            stringBuilder.append(" WHERE id='"+object.getId()+"'");

            return stringBuilder.toString();
        } else {
            return null;
        }
    }

    private String getTableName(Class<?> clazz) {
        if (clazz.isAnnotationPresent(TableName.class)) {
            // 获取类上的自定义注解
            TableName tableNameAnnotation = clazz.getAnnotation(TableName.class);
            return tableNameAnnotation.value();
        } else {
            // 如果没有自定义注解，可以根据业务逻辑处理此情况
            return null;
        }
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        } else if (value instanceof String) {
            return "'" + value + "'";
        } else {
            return value.toString();
        }
    }

    @Override
    public FunctionEnum getFunctionEnum() {
        return FunctionEnum.UPDATE;
    }
}
