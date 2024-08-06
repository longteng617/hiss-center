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
 * @author: wgl
 * @describe: 创建表字段sql的转换器
 * @date: 2022/12/28 10:10
 */
@Component
public class CreateSqlHandler implements HissSqlExchangeHandler {

    /**
     * CREATE TABLE tableName (
     *                                    `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
     *                                    `created_time` datetime DEFAULT NULL COMMENT '创建时间',
     *                                    `category` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分组',
     *                                    `tenant_id` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '租户',
     *                                    `config_resource_id` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '表单配置关联ID',
     *                                    `name` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '表单名称',
     *                                    `version` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '版本',
     *                                    `status` tinyint(4) DEFAULT NULL COMMENT '是否有效 1是，0否',
     *                                    PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='表单设计基本信息';
     * @param objects
     * @return
     */
    @Override
    public String exchangeSql(BasePojo... objects) {
        if (objects.length == 0) {
            return null;
        }
        StringBuilder sqlBuilder = new StringBuilder();
        for (BasePojo index: objects) {
            FormDetailPojo formDetailPojo = (FormDetailPojo)index;
            // 获取表名
            sqlBuilder.append("CREATE TABLE ");
            sqlBuilder.append(getTableName(formDetailPojo));
            sqlBuilder.append(" ( ");
            //默认字段设置
            setDefaultColumn(formDetailPojo,sqlBuilder);
            List<String> columns = new ArrayList<String>();
            List<HissFormTableFields> fields = formDetailPojo.getFields();
            for (HissFormTableFields field : fields) {
                String columnName = field.getPhysicalName();
                String columnType = getColumnType(field.getFieldType());
                String columnDefinition = columnName + " " + columnType;
                columns.add(columnDefinition);
            }
            sqlBuilder.append(String.join(",", columns));
            sqlBuilder.append(" PRIMARY KEY (`id`) ");
            sqlBuilder.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT=");
            sqlBuilder.append("'"+formDetailPojo.getTableMsg()+"';");
        }
        return sqlBuilder.toString();
    }

    /**
     * 设置默认字段内容
     *
     * @param formDetailPojo
     * @param sqlBuilder
     * @return
     */
    private void setDefaultColumn(FormDetailPojo formDetailPojo, StringBuilder sqlBuilder) {
        sqlBuilder.append("`id` varchar(36) NOT NULL COMMENT '主键',");
        sqlBuilder.append(" `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',");
        sqlBuilder.append(" `parent_id` varchar(36) COMMENT '父表数据id',");
        sqlBuilder.append(" `user_id` varchar(36) COMMENT '操作用户Id',");
        sqlBuilder.append(" `table_id` varchar(64) DEFAULT '"+formDetailPojo.getId()+"' COMMENT '关联表id',");
        sqlBuilder.append(" `form_id` varchar(64) DEFAULT '"+formDetailPojo.getModelId()+"' COMMENT '表单Id',");
    }

    @Override
    public FunctionEnum getFunctionEnum() {
        return FunctionEnum.CREATE;
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


    /**
     * 获取字段类型从
     * @param fieldType
     * @return
     */
    protected String getColumnType(String fieldType) {
        if (fieldType.equals("Integer") || fieldType.equals("int")) {
            return "INT";
        } else if (fieldType.equalsIgnoreCase("Long")) {
            return "BIGINT";
        } else if (fieldType.equalsIgnoreCase("Float")) {
            return "FLOAT";
        } else if (fieldType.equalsIgnoreCase("Double") ) {
            return "DOUBLE";
        } else if (fieldType.equalsIgnoreCase("Boolean")) {
            return "BOOLEAN";
        } else if (fieldType.equalsIgnoreCase("Text")) {
            return "TEXT";
        } else if (fieldType.equalsIgnoreCase("String")) {
            return "VARCHAR(255)";
        } else if (fieldType.equalsIgnoreCase("Date")||fieldType.equalsIgnoreCase("LocalDateTime")||fieldType.equalsIgnoreCase("LocalDate")) {
            return "DATETIME";
        } else if (fieldType.equalsIgnoreCase("enum")) {
            return "VARCHAR(255)";
        }
        //TODO 如果有其他类型在这里添加

        return "VARCHAR(255)"; // 默认使用 VARCHAR(255)
    }

}
