package cn.itcast.hiss.form.mapper.drive.impl;

import cn.itcast.hiss.common.dtos.Complex;
import cn.itcast.hiss.form.annocation.FieldName;
import cn.itcast.hiss.form.exceptions.SqlException;
import cn.itcast.hiss.form.exceptions.StrException;
import cn.itcast.hiss.form.mapper.drive.DatabaseDriver;
import cn.itcast.hiss.form.pojo.BasePojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * MysqlDatabaseDriver
 *
 * @author: wgl
 * @describe: mysql连接驱动
 * @date: 2022/12/28 10:10
 */
@Component
public class MySQLDatabaseDriver implements DatabaseDriver<Connection> {

    private static Connection connection = null;
    @Autowired
    DataSource dataSource;

    /**
     * 建立连接
     * @return
     */
    @Override
    public Connection connect() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("获取数据库连接失败!....");
        }
    }

    public Connection getConnection(){
        if(connection!=null){
            try {
                if(connection.isClosed()){
                    connection = null;
                }
            } catch (SQLException e) {
                connection = null;
            }
        }
        if(connection==null){
            synchronized (MySQLDatabaseDriver.class){
                if(connection==null){
                    synchronized (MySQLDatabaseDriver.class){
                        connection = connect();
                    }
                }
            }
        }
        return connection;
    }

    /**
     * 重连
     */
    @Override
    public void reConnection() {
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void executeSql(String sql) throws StrException {
        Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            boolean execute = stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SqlException("sql执行异常");
        }
    }

    @Override
    public int selectCountSql(String sql) {
        Connection con = getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet resultSet = stmt.executeQuery(sql);
            resultSet.getInt(0);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SqlException("sql执行异常");
        }
        return 0;
    }

    /**
     * 执行Insert并且返回插入数据的ID
     * @param sql
     * @return
     */
    @Override
    public Long executeInsertAndReturnId(String sql) {
        Connection con = getConnection();
        try (PreparedStatement statement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 查询一条数据
     * @param sql
     * @param clazz
     * @return
     */
    @Override
    public <T extends BasePojo> T queryOne(String sql, Class<T> clazz) {
        Connection con = getConnection();
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                T object = clazz.newInstance();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Field field = getFieldByColumnName(clazz, columnName);
                    if (field != null) {
                        field.setAccessible(true);
                        Class<?> fieldType = field.getType();
                        Object value = resultSet.getObject(i);
                        Object convertedValue = convertValue(value, fieldType);
                        field.set(object, convertedValue);
                    }
                }
                return object;
            }
        } catch (SQLException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }


    private Field getFieldByColumnName(Class<?> clazz, String columnName) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(FieldName.class)) {
                    FieldName fieldNameAnnotation = field.getAnnotation(FieldName.class);
                    if (fieldNameAnnotation.value().equals(columnName)) {
                        return field;
                    }
                } else if (field.getName().equals(columnName)) {
                    return field;
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

    /**
     * 批量查询的接口
     * @param sql
     * @param clazz
     * @return
     * @param <T2>
     */
    @Override
    public <T2 extends BasePojo> List<T2> queryList(String sql, Class<T2> clazz) {
        Connection con = getConnection();
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            List<T2> list = new ArrayList<>();
            while (resultSet.next()) {
                T2 object = clazz.newInstance();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Field field = getFieldByColumnName(clazz, columnName);
                    if (field != null) {
                        field.setAccessible(true);
                        Class<?> fieldType = field.getType();
                        Object value = resultSet.getObject(i);
                        Object convertedValue = convertValue(value, fieldType);
                        field.set(object, convertedValue);
                    }
                }
                list.add(object);
            }
            return list;
        } catch (SQLException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 执行N条sql
     * @param sqls
     * @return
     * @throws SQLException
     */
    @Override
    public int executeSql(List<Complex<String, List>> sqls) throws Exception {
        Connection con = getConnection();
        con.setAutoCommit(false);
        try {
            int count = 0;
            for (Complex<String, List> sql : sqls) {
                PreparedStatement statement = con.prepareStatement(sql.getFirst());
                List second = sql.getSecond();
                if (second != null) {
                    for (int i = 0; i < second.size(); i++) {
                        statement.setObject(i+1, second.get(i));
                    }
                }
                if(statement.execute()){
                    count++;
                }
            }
            con.commit();
            return count;
        }catch (Exception e){
            e.printStackTrace();
            con.rollback();
            throw e;
        }finally {
            con.setAutoCommit(true);
        }
    }

    /**
     * 根据字段名获取字段对象（包括父类）
     * @param clazz
     * @param fieldName
     * @return
     */
    private Field getField(Class<?> clazz, String fieldName) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                field = getField(superClass, fieldName);
            }
        }
        return field;
    }

    /**
     * 将值转换为指定类型
     * @param value
     * @param targetType
     * @return
     */
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.equals(value.getClass())) {
            return value;
        }
        if (targetType.equals(String.class)) {
            if (value instanceof byte[]) {
                return new String((byte[]) value);
            } else {
                return value.toString();
            }
        }
        if (targetType.equals(Long.class)) {
            return Long.valueOf(value.toString());
        }
        if (targetType.equals(Integer.class)) {
            return Integer.valueOf(value.toString());
        }
        if (targetType.equals(Boolean.class)) {
            // 处理 Boolean 类型字段的特殊情况，将 Integer 类型转换为 Boolean 类型
            Boolean booleanValue = (Integer) value != 0;
            return booleanValue;
        }
        if (targetType.equals(Timestamp.class) && value instanceof java.util.Date) {
            return new Timestamp(((java.util.Date) value).getTime());
        }
        if (targetType.equals(LocalDateTime.class)) {
            return ((Timestamp) value).toLocalDateTime();
        }
        if (targetType.equals(java.util.Date.class)&& value instanceof LocalDateTime) {
            try {
                LocalDateTime localDateTime = (LocalDateTime) value;
                return java.util.Date.from(localDateTime.atZone( ZoneId.systemDefault()).toInstant());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        // 其他类型转换逻辑...
        return value;
    }
}
