package cn.itcast.hiss.process.activiti.handler.task;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.task.AnyJumpTask;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.task.AnyJumpTaskMessage;
import cn.itcast.hiss.process.activiti.service.ActivitiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Task_JumpTaskHandler
 *
 * @author: miukoo
 * @describe: 撤回
 * @date: 2022/12/28 10:10
 */
@Slf4j
@Component
public class Task_WithdrawTaskHandler implements CmdHandler<AnyJumpTaskMessage> {

    @Autowired
    private ActivitiService activitiService;

    /**
     * 跳转功能实现
     *
     * @param params
     * @param messageContext
     */
    @Override
    public void invoke(Message params, MessageContext messageContext) {
        AnyJumpTask palyload = (AnyJumpTask) params.getPalyload();
        if(StrUtil.isEmpty( palyload.getOperatorName())){
            palyload.setOperatorName("撤回");
        }
        String jumpTaskOrActivityId = palyload.getJumpTaskOrActivityId();
        if("#start".equals(jumpTaskOrActivityId)){
            palyload.setJumpTaskOrActivityId(null);
            activitiService.withdrawTask((AnyJumpTaskMessage) params,messageContext,true);
        }else if("#prev".equals(jumpTaskOrActivityId)){
            palyload.setJumpTaskOrActivityId(null);
            activitiService.withdrawTask((AnyJumpTaskMessage) params,messageContext,false);
        }else{
            activitiService.withdrawTask((AnyJumpTaskMessage) params,messageContext,false);
        }
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.TASK_WITHDRAW_TASK.getId();
    }


}
