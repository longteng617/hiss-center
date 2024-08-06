package cn.itcast.hiss.form.sql.impl;

import cn.itcast.hiss.form.pojo.BasePojo;
import cn.itcast.hiss.form.pojo.FormDetailPojo;
import cn.itcast.hiss.form.pojo.HissFormTableFields;
import cn.itcast.hiss.form.sql.enums.FunctionEnum;
import cn.itcast.hiss.form.sql.manage.HissSqlExchangeHandler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * CreateSqlHandler
 *
 * @author: miukoo
 * @describe: 给表增加一个字段
 * @date: 2022/12/28 10:10
 */
@Component
public class AlertSqlHandler extends CreateSqlHandler implements HissSqlExchangeHandler {

    @Override
    public String exchangeSql(BasePojo... objects) {
        if (objects.length == 0) {
            return null;
        }
        StringBuilder sqlBuilder = new StringBuilder();
        for (BasePojo index: objects) {
            FormDetailPojo formDetailPojo = (FormDetailPojo)index;
            // 获取表名
            sqlBuilder.append("ALTER TABLE ");
            sqlBuilder.append(getTableName(formDetailPojo));
            sqlBuilder.append(" ADD COLUMN ");
            //默认字段设置
            List<String> columns = new ArrayList<String>();
            List<HissFormTableFields> fields = formDetailPojo.getFields();
            for (HissFormTableFields field : fields) {
                String columnName = field.getPhysicalName();
                String columnType = getColumnType(field.getFieldType());
                String columnDefinition = columnName + " " + columnType;
                columns.add(columnDefinition);
            }
            sqlBuilder.append(String.join(",", columns));
        }
        return sqlBuilder.toString();
    }

    @Override
    public FunctionEnum getFunctionEnum() {
        return FunctionEnum.ALTER;
    }

    /**
     * 获取表名
     * @param formDetailPojo
     * @return
     */
    private String getTableName(FormDetailPojo formDetailPojo) {
        String tableName = formDetailPojo.getTableName();
        return "`"+tableName+"`";
    }

}
