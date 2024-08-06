package cn.itcast.hiss.process.activiti.handler.task;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.task.CancelTask;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.task.CancelTaskMessage;
import cn.itcast.hiss.process.activiti.service.ActivitiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * CancelHandler
 *
 * @author: miukoo
 * @describe: 取消流程
 * @date: 2022/12/28 10:10
 */
@Component
public class Task_CancelHandler implements CmdHandler<CancelTaskMessage> {

    @Autowired
    private ActivitiService activitiService;

    /**
     * 取消任务
     *
     * @param params
     * @param messageContext
     */
    @Override
    public void invoke(Message params, MessageContext messageContext) {
        CancelTask palyload = (CancelTask) params.getPalyload();
        if (StrUtil.isEmpty(palyload.getOperatorName())) {
            palyload.setOperatorName("取消");
        }
        activitiService.cancelTask((CancelTaskMessage) params, messageContext);
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.TASK_CANCEl_TASK.getId();
    }
}
