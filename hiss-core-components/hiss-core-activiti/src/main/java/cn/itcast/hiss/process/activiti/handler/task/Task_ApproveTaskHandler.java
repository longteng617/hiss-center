package cn.itcast.hiss.process.activiti.handler.task;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.task.ApproveTask;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.task.ApproveTaskMessage;
import cn.itcast.hiss.process.activiti.service.ActivitiService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

/**
 * Task_ApproveTaskHandler
 * <p>
 * 完成待办任务
 */
@Component
@Slf4j
public class Task_ApproveTaskHandler implements CmdHandler<ApproveTaskMessage> {

    @Autowired
    private ActivitiService activitiService;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        ApproveTask payload = (ApproveTask) params.getPalyload();
        if (StrUtil.isEmpty(payload.getOperatorName())) {
            payload.setOperatorName("同意");
        }
        Map<String, Object> variables = payload.getVariables();
        if(variables!=null){
            for (String key : variables.keySet()) {
                String value = (String) variables.get(key);
                if(value.startsWith("list#")){
                    String[] split = value.replace("list#", "").split(",");
                    variables.put(key, Arrays.asList(split));
                }
            }
        }
        payload.setVariables(variables);
        activitiService.approveTask((ApproveTaskMessage) params, messageContext);
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.TASK_APPROVE_TASK.getId();
    }
}
