package cn.itcast.hiss.process.activiti.handler.task;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.task.ReceivedTask;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.task.ReceivedTaskMessage;
import cn.itcast.hiss.process.activiti.service.ActivitiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Task_SignalEventReceivedTaskHandler
 *
 * @author: wgl
 * @describe: 触发
 * @date: 2022/12/28 10:10
 */
@Component
@Slf4j
public class Task_ReceivedHandler implements CmdHandler<ReceivedTaskMessage> {

    @Autowired
    private ActivitiService activitiService;

    /**
     * 拾取任务
     *
     * @param params
     * @param messageContext
     */
    @Override
    public void invoke(Message params, MessageContext messageContext) {
        ReceivedTask palyload = (ReceivedTask) params.getPalyload();
        if (StrUtil.isEmpty(palyload.getOperatorName())) {
            palyload.setOperatorName("触发");
        }
        activitiService.received((ReceivedTaskMessage) params, messageContext);
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.TASK_RECEIVED_TASK.getId();
    }
}
