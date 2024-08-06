package cn.itcast.hiss.form.mapper.drive;

import cn.itcast.hiss.common.dtos.Complex;
import cn.itcast.hiss.form.pojo.BasePojo;

import java.sql.SQLException;
import java.util.List;

/**
 * DatabaseDriver
 *
 * @author: wgl
 * @describe: TODO
 * @date: 2022/12/28 10:10
 */
public interface DatabaseDriver<T> {
    /**
     * 建立连接
     *
     * @return
     */
    T connect();


    /**
     * 重连
     */
    void reConnection();


    /**
     * 执行sql
     */
    void executeSql(String sql);

    int selectCountSql(String sql);

    /**
     * 执行insert 并返回插入数据的id
     * @param sql
     * @return
     */
    Long executeInsertAndReturnId(String sql);

    /**
     * 获取连接
     *
     * @return
     */
    T getConnection();

    <T2 extends BasePojo> T2 queryOne(String sql, Class<T2> clazz);

    <T2 extends BasePojo> List<T2> queryList(String sql, Class<T2> clazz);

    int executeSql(List<Complex<String, List>> sqls) throws Exception;
}

