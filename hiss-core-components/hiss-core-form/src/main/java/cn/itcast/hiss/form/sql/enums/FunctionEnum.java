package cn.itcast.hiss.form.sql.enums;

/**
 * FunctionEnum
 *
 * @author: wgl
 * @describe: Sql类型构建器
 * @date: 2022/12/28 10:10
 */
public enum FunctionEnum {
    UPDATE,//更新
    DELETE,//删除
    CREATE,//创建
    ALTER,//创建
    LIST,//列表
    COUNT,//统计
    PAGE,//分页
    INSERT,//插入
    SELECT,//查询

    BATCH_INSERT,
    DYNAMIC_FIELDS,//动态字段添加
    DYNAMIC_FIELDS_INSERT,
}
