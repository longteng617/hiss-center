package cn.itcast.hiss.process.activiti.handler.task;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.task.NotificationTask;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.task.NotificationTaskMessage;
import cn.itcast.hiss.process.activiti.service.ActivitiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 抄送某个人
 * @author: miukoo
 * @date: 2022/12/28 10:10
 */
@Component
public class Task_CcTaskHandler implements CmdHandler<NotificationTaskMessage> {

    @Autowired
    private ActivitiService activitiService;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        NotificationTaskMessage message = (NotificationTaskMessage) params;
        NotificationTask palyload = message.getPalyload();
        if(StrUtil.isEmpty( palyload.getOperatorName())){
            palyload.setOperatorName("抄送");
        }
        activitiService.createCcTask(message,messageContext);
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.TASK_CC_TASK.getId();
    }
}
