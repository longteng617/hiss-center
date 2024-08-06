package cn.itcast.hiss.form.validate.impl;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.form.ArgsValidated;
import cn.itcast.hiss.form.mapper.HissFormModelMapper;
import cn.itcast.hiss.form.mapper.HissFormTableFieldsMapper;
import cn.itcast.hiss.form.mapper.HissFormTablesMapper;
import cn.itcast.hiss.form.mapper.drive.DatabaseDriver;
import cn.itcast.hiss.form.pojo.HissFormModel;
import cn.itcast.hiss.form.pojo.HissFormTableFields;
import cn.itcast.hiss.form.pojo.HissFormTables;
import cn.itcast.hiss.form.service.HissFormModelService;
import cn.itcast.hiss.form.validate.ArgValidate;
import cn.itcast.hiss.form.validate.FeedbackType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DataOlnyValidate
 *
 * @author: miukoo
 * @describe: 数据唯一性校验
 * @date: 2022/12/28 10:10
 */
@Component
public class DataOlnyValidate implements ArgValidate {
    @Autowired
    HissFormModelMapper hissFormModelMapper;
    @Autowired
    HissFormTableFieldsMapper hissFormTableFieldsMapper;
    @Autowired
    HissFormTablesMapper hissFormTablesMapper;
    @Autowired
    DatabaseDriver databaseDriver;

    @Autowired
    HissFormModelService hissFormModelService;

    @Override
    public boolean validate(ArgsValidated palyload) {
        HissFormModel hissFormModel = hissFormModelMapper.selectById(palyload.getModelId());
        if(hissFormModel!=null){
            List<HissFormTableFields> fields = hissFormTableFieldsMapper.listByModelId(hissFormModel.getId());
            HissFormTableFields hissFormTableFields = hissFormModelService.findHissFormTableFields(fields, palyload.getFiledId());
            if(hissFormTableFields!=null){
                HissFormTables hissFormTables = hissFormTablesMapper.selectById(hissFormTableFields.getTableId());
                if(hissFormTables!=null){
                    String sql = String.format("select count(1) from %s where `%s`='%s'",
                            hissFormTables.getTableName(),
                            hissFormTableFields.getPhysicalName(),
                            palyload.getArgs()
                    );
                    if(StrUtil.isNotEmpty(palyload.getDataId())){
                        sql+= String.format(" and id='%s'" ,palyload.getArgs().replace("'","\\'") );
                    }
                    int count = databaseDriver.selectCountSql(sql);
                    return count>0;
                }
            }
        }
        return false;
    }

    @Override
    public String getType() {
        return FeedbackType.ONLY_ONE_VALUE.name();
    }
}
