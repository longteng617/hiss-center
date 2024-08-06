package cn.itcast.hiss.process.activiti.handler.task;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.task.HissTask;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.task.TaskMessage;
import cn.itcast.hiss.process.activiti.service.ActivitiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

;

/**
 * ClaimTaskHandler
 *
 * @author: wgl
 * @describe: 拾取任务
 * @date: 2022/12/28 10:10
 */
@Component
public class Task_ClaimHandler implements CmdHandler<TaskMessage> {

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
        HissTask palyload = (HissTask) params.getPalyload();
        if(StrUtil.isEmpty( palyload.getOperatorName())){
            palyload.setOperatorName("认领");
        }
        activitiService.claimTask((TaskMessage)params,messageContext);
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.TASK_CLAIM_TASK.getId();
    }
}
