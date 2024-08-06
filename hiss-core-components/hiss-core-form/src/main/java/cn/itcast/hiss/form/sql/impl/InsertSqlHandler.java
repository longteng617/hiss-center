package cn.itcast.hiss.form.sql.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.itcast.hiss.form.annocation.FieldName;
import cn.itcast.hiss.form.annocation.TableName;
import cn.itcast.hiss.form.pojo.BasePojo;
import cn.itcast.hiss.form.sql.enums.FunctionEnum;
import cn.itcast.hiss.form.sql.manage.HissSqlExchangeHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * InsertSqlHandler
 *
 * @author: wgl
 * @describe: 插入sql转换器
 * @date: 2022/12/28 10:10
 */
@Component
public class InsertSqlHandler implements HissSqlExchangeHandler {

    @Override
    public String exchangeSql(BasePojo... objects) {
        StringBuilder stringBuilder = new StringBuilder();
        if (CollectionUtil.isNotEmpty(CollectionUtil.newArrayList(objects))) {
            Class<?> clazz = objects[0].getClass();

            // 获取父类和子类的所有字段
            List<Field> allFields = new ArrayList<>();
            Class<?> currentClass = clazz;
            while (currentClass != null) {
                Field[] fields = currentClass.getDeclaredFields();
                allFields.addAll(Arrays.asList(fields));
                currentClass = currentClass.getSuperclass();
            }

            stringBuilder.append("INSERT INTO ");
            stringBuilder.append(getTableName(clazz));
            stringBuilder.append("(");

            List<String> columns = new ArrayList<>();
            List<String> values = new ArrayList<>();

            for (BasePojo object : objects) {
                List<String> objectValues = new ArrayList<>();
                for (Field field : allFields) {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(object);
                        if (value != null) {
                            //忽略附加字段内容
                            if(field.getName().equals("params")){
                                continue;
                            }
                            if (field.isAnnotationPresent(FieldName.class)) {
                                FieldName fieldNameAnnotation = field.getAnnotation(FieldName.class);
                                String columnName = fieldNameAnnotation.value();
                                if (!columns.contains(columnName)) {
                                    columns.add(columnName);
                                }
                            } else {
                                columns.add(field.getName());
                            }
                            objectValues.add(formatValue(value));
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                values.add("(" + String.join(",", objectValues) + ")");
            }

            stringBuilder.append(String.join(",", columns));
            stringBuilder.append(") VALUES ");
            stringBuilder.append(String.join(",", values));

            return stringBuilder.toString();
        } else {
            return null;
        }
    }

    private String getTableName(Class<?> clazz) {
        if (clazz.isAnnotationPresent(TableName.class)) {
            TableName tableNameAnnotation = clazz.getAnnotation(TableName.class);
            return tableNameAnnotation.value();
        } else {
            return convertObjectNameToTableName(clazz.getSimpleName());
        }
    }

    private String convertObjectNameToTableName(String objectName) {
        String[] words = objectName.split("(?=[A-Z])");
        return String.join("_", words).toLowerCase();
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        } else if (value instanceof String) {
            return "'" + value + "'";
        } else if (value instanceof Date) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return "'" + formatter.format((Date) value) + "'";
        } else if (value instanceof Enum<?>) {
            return "'" + ((Enum<?>) value).name() + "'";
        } else {
            return value.toString();
        }
    }


    @Override
    public FunctionEnum getFunctionEnum() {
        return FunctionEnum.INSERT;
    }
}