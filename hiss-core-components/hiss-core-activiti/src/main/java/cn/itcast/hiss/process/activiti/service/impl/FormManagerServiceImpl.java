package cn.itcast.hiss.process.activiti.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.form.FormDataList;
import cn.itcast.hiss.common.dtos.PageResponseResult;
import cn.itcast.hiss.form.mapper.DynamicTableMapper;
import cn.itcast.hiss.form.mapper.HissFormTablesMapper;
import cn.itcast.hiss.form.pojo.HissFormTables;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.form.FormDataListMessage;
import cn.itcast.hiss.process.activiti.mapper.HissProcessFormMapper;
import cn.itcast.hiss.process.activiti.mapper.HissSystemUserMapper;
import cn.itcast.hiss.process.activiti.service.FormManagerService;
import cn.itcast.hiss.process.activiti.util.AdminUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/*
 * @author miukoo
 * @description 表单的CRUD
 * @date 2023/7/10 20:27
 * @version 1.0
 **/
@Transactional
@Service
public class FormManagerServiceImpl implements FormManagerService {

    @Autowired
    private HissFormTablesMapper hissFormTablesMapper;

    @Autowired
    private DynamicTableMapper dynamicTableMapper;

    @Autowired
    private HissSystemUserMapper hissSystemUserMapper;

    @Autowired
    private HissProcessFormMapper hissProcessFormMapper;

    /**
     * 查询一张表的列表数据
     * @param params
     * @param messageContext
     */
    @Override
    public void queryFormDataList(FormDataListMessage params, MessageContext messageContext) {
        FormDataList palyload = params.getPalyload();
        HissFormTables hissFormTables = hissFormTablesMapper.selectById(palyload.getTableId());
        if(hissFormTables!=null){
            if(hissFormTables.getModelId().equals(palyload.getFormId())){
                palyload.checkParam();
                Long current = palyload.getCurrent();
                palyload.setCurrent((palyload.getCurrent()-1)*palyload.getPageSize());
                List<Map> data = dynamicTableMapper.listTableDataForPage(hissFormTables, palyload);
                long total = dynamicTableMapper.countTableDataForPage(hissFormTables, palyload);
                PageResponseResult responseResult = new PageResponseResult(current,palyload.getPageSize(),total);
                responseResult.setData(data);
                messageContext.addResultAndCount("result",responseResult);
            }else{
                messageContext.addError("msg","无权限读取该表数据");
            }
        }else{
            messageContext.addError("msg","未找到对应单");
        }
    }

    /**
     * 删除一条数据
     * @param params
     * @param messageContext
     */
    @Override
    public void deleteFormData(FormDataListMessage params, MessageContext messageContext) {
        FormDataList palyload = params.getPalyload();
        HissFormTables hissFormTables = hissFormTablesMapper.selectById(palyload.getTableId());
        if(hissFormTables!=null){
            if(StrUtil.isNotEmpty(palyload.getDataId())) {
                boolean canDelete = AdminUtil.isAdmin(params);// 超级管理员可删除
                if(!canDelete){ // 应用管理员可删除
                    canDelete = hissSystemUserMapper.selectById(params.getMessageAuth().getCurrentUser().getUserId())==null;
                }
                if(!canDelete) {// 自己是否可删除
                    Map map = dynamicTableMapper.listTableDataById(hissFormTables, palyload.getDataId());
                    if (map != null) {
                        String userId = (String) map.get("user_id");
                        canDelete = params.getMessageAuth().getCurrentUser().getUserId().equals(userId);
                    }
                }
                if (canDelete) {
                    // 判断数据是否被流程使用了
                    int count = hissProcessFormMapper.countByDataId(palyload.getDataId());
                    if(count==0){
                        dynamicTableMapper.deleteById(hissFormTables,palyload.getDataId());
                        messageContext.addResultAndCount("msg", "删除成功");
                    }else {
                        messageContext.addError("msg", "数据已经被流程使用，不允许删除");
                    }
                } else {
                    messageContext.addError("msg", "无权限删除表数据");
                }
            }else {
                messageContext.addError("msg","未找到对应数据");
            }
        }else{
            messageContext.addError("msg","未找到对应表单");
        }
    }
}
