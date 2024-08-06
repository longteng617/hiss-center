package cn.itcast.hiss.process.activiti.handler.client.process;

import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sys.ProcessInstanceMessage;
import cn.itcast.hiss.process.activiti.service.ProcessApplyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 *
 * @author: miukoo
 * @describe: 删除个人申请
 * @date: 2022/12/28 10:10
 */
@Component
@Slf4j
public class CLI_ProcessApplyDeleteHandler implements CmdHandler<ProcessInstanceMessage> {
    @Autowired
    private ProcessApplyService processApplyService;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        processApplyService.deleteProcessApply((ProcessInstanceMessage) params,messageContext);
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.CLI_PROCESS_APPLY_DELETE.getId();
    }
}
