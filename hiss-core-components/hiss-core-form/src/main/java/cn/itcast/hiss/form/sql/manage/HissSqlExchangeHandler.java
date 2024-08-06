package cn.itcast.hiss.form.sql.manage;

import cn.itcast.hiss.form.pojo.BasePojo;
import cn.itcast.hiss.form.sql.enums.FunctionEnum;

/**
 * SqlExchangeHandler
 *
 * @author: wgl
 * @describe: Sql转换器统一模板
 * @date: 2022/12/28 10:10
 */
public interface HissSqlExchangeHandler {

    public String exchangeSql(BasePojo... object);

    FunctionEnum getFunctionEnum();


}
