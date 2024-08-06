package cn.itcast.hiss.process.activiti.handler.task;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.task.RejectTask;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.task.RejectTaskMessage;
import cn.itcast.hiss.process.activiti.service.ActivitiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Task_RejectTaskHandler
 *
 * @author: wgl
 * @describe: 不同意任务
 * @date: 2022/12/28 10:10
 */
@Component
public class Task_RejectTaskHandler implements CmdHandler<RejectTaskMessage> {

    @Autowired
    private ActivitiService activitiService;

    /**
     * 不同意任务
     *
     * @param params
     * @param messageContext
     */
    @Override
    public void invoke(Message params, MessageContext messageContext) {
        RejectTask payload = (RejectTask) params.getPalyload();
        if(StrUtil.isEmpty( payload.getOperatorName())){
            payload.setOperatorName("不同意");
        }
        activitiService.rejectTask((RejectTaskMessage)params,messageContext);

    }

    /**
     * 获取id
     *
     * @return
     */
    @Override
    public String getId() {
        return HandlerIdClientEnum.TASK_REJECT_TASK.getId();
    }
}
