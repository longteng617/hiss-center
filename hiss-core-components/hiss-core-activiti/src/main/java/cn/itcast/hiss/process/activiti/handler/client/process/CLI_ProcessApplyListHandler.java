package cn.itcast.hiss.process.activiti.handler.client.process;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.common.dtos.PageResponseResult;
import cn.itcast.hiss.common.enums.ModelTypeEnum;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sys.ProcessInstanceMessage;
import cn.itcast.hiss.message.sys.ProcessModelMessage;
import cn.itcast.hiss.message.sys.pojo.ProcessModel;
import cn.itcast.hiss.process.activiti.mapper.ActReModelMapper;
import cn.itcast.hiss.process.activiti.pojo.ActReModel;
import cn.itcast.hiss.process.activiti.service.ProcessApplyService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 *
 * @author: miukoo
 * @describe: 申请人的流程查询
 * @date: 2022/12/28 10:10
 */
@Component
@Slf4j
public class CLI_ProcessApplyListHandler implements CmdHandler<ProcessInstanceMessage> {
    @Autowired
    private ProcessApplyService processApplyService;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        processApplyService.listProcessApply((ProcessInstanceMessage) params,messageContext);
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.CLI_PROCESS_APPLY_LIST.getId();
    }
}
