package cn.itcast.hiss.process.activiti.handler.task;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.task.AddExecutionTask;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.task.AddExecutionTaskMessage;
import cn.itcast.hiss.process.activiti.service.ActivitiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Task_ParallelSignTaskHandler
 *
 * @author: wgl
 * @describe: 并行加签任务
 * @date: 2022/12/28 10:10
 */
@Component
public class Task_ParallelSignTaskHandler implements CmdHandler<AddExecutionTaskMessage> {

    @Autowired
    private ActivitiService activitiService;

    /**
     * 并行加签任务
     *
     * @param params
     * @param messageContext
     */
    @Override
    public void invoke(Message params, MessageContext messageContext) {
        AddExecutionTask palyload = (AddExecutionTask) params.getPalyload();
        if (StrUtil.isEmpty(palyload.getOperatorName())) {
            palyload.setOperatorName("加签");
        }
        activitiService.doParallelSignTask((AddExecutionTaskMessage) params, messageContext);
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.TASK_PARALLEL_SIGN_TASK.getId();
    }
}
