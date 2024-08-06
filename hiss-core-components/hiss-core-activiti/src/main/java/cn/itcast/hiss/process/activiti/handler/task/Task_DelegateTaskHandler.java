package cn.itcast.hiss.process.activiti.handler.task;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.task.DelegateTask;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.task.DelegateTaskMessage;
import cn.itcast.hiss.process.activiti.service.ActivitiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Task_DelegateTaskHandler
 *
 * @author: wgl
 * @describe: 交办任务
 * @date: 2022/12/28 10:10
 */
@Component
public class Task_DelegateTaskHandler implements CmdHandler<DelegateTaskMessage> {

    @Autowired
    private ActivitiService activitiService;

    /**
     * 交办任务
      * @param params
     * @param messageContext
     */
    @Override
    public void invoke(Message params, MessageContext messageContext) {
        DelegateTask delegateTask = (DelegateTask) params.getPalyload();
        if(StrUtil.isEmpty( delegateTask.getOperatorName())){
            delegateTask.setOperatorName("委派");
        }
        activitiService.delegateTask((DelegateTaskMessage)params,messageContext);
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.TASK_DELEGATE_TASK.getId();
    }
}
