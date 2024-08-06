package cn.itcast.hiss.process.activiti.handler.client.process;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.common.dtos.PageResponseResult;
import cn.itcast.hiss.common.enums.ModelTypeEnum;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sys.ProcessModelMessage;
import cn.itcast.hiss.message.sys.pojo.ProcessModel;
import cn.itcast.hiss.process.activiti.mapper.ActReModelMapper;
import cn.itcast.hiss.process.activiti.pojo.ActReModel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 *
 * @author: miukoo
 * @describe: 流程设计查询处理器
 * @date: 2022/12/28 10:10
 */
@Component
@Slf4j
public class CLI_ProcessModelListHandler implements CmdHandler<ProcessModelMessage> {
    @Autowired
    private ActReModelMapper actReModelMapper;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        ProcessModel processModel = (ProcessModel) params.getPalyload();
        LambdaQueryWrapper<ActReModel> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ActReModel::getKey,processModel.getType().name());
        wrapper.eq(ActReModel::getTenantId,processModel.getUserAppId());
        if(StrUtil.isNotEmpty(processModel.getCategory())) {
            wrapper.eq(ActReModel::getCategory, processModel.getCategory());
        }
        if(StrUtil.isNotEmpty(processModel.getName())){
            wrapper.like(ActReModel::getName,processModel.getName());
        }
        processModel.checkParam();
        Page<ActReModel> page = Page.of(processModel.getCurrent(),processModel.getPageSize());
        page = actReModelMapper.selectPage(page,wrapper);
        long total = page.getTotal();
        PageResponseResult pageResponseResult = new PageResponseResult(processModel.getCurrent(),processModel.getPageSize(),total);
        // 查询分页数据
        pageResponseResult.setData(page.getRecords());
        messageContext.addResultAndCount("result", pageResponseResult);
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.CLI_PROCESS_MODEL_LIST.getId();
    }
}
