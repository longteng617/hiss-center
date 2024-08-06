package cn.itcast.hiss.process.activiti.handler.client.form;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.common.dtos.PageResponseResult;
import cn.itcast.hiss.form.mapper.HissFormModelMapper;
import cn.itcast.hiss.form.pojo.HissFormModel;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sys.ProcessModelMessage;
import cn.itcast.hiss.message.sys.pojo.ProcessModel;
import cn.itcast.hiss.process.activiti.mapper.HissUserAppMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 *
 * @author: miukoo
 * @describe: 表单设计查询处理器
 * @date: 2022/12/28 10:10
 */
@Component
@Slf4j
public class CLI_FormModelListHandler implements CmdHandler<ProcessModelMessage> {

    @Autowired
    private HissUserAppMapper hissUserAppMapper;
    @Autowired
    private HissFormModelMapper hissFormModelMapper;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        ProcessModel processModel = (ProcessModel) params.getPalyload();

        LambdaQueryWrapper<HissFormModel> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(HissFormModel::getTenantId,processModel.getUserAppId());
        if(StrUtil.isNotEmpty(processModel.getCategory())) {
            wrapper.eq(HissFormModel::getCategory, processModel.getCategory());
        }
        boolean canQuery = isAdmin(params);
        if(!canQuery){
            List<String> userAppIdsList = hissUserAppMapper.getUserAppIdsList(processModel.getAdminId());
            canQuery = userAppIdsList.contains(processModel.getUserAppId());
        }
        if(!canQuery){
            messageContext.addError("msg","无权限操作");
            return;
        }
        if(StrUtil.isNotEmpty(processModel.getName())){
            wrapper.like(HissFormModel::getName,processModel.getName());
        }
        processModel.checkParam();
        Page<HissFormModel> page = Page.of(processModel.getCurrent(),processModel.getPageSize());
        page = hissFormModelMapper.selectPage(page,wrapper);
        long total = page.getTotal();
        PageResponseResult pageResponseResult = new PageResponseResult(processModel.getCurrent(),processModel.getPageSize(),total);
        // 查询分页数据
        pageResponseResult.setData(page.getRecords());
        messageContext.addResultAndCount("result", pageResponseResult);
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.CLI_FORM_MODEL_LIST.getId();
    }
}
