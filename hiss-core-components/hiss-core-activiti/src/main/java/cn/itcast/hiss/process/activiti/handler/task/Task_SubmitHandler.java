package cn.itcast.hiss.process.activiti.handler.task;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.form.FormSubmitData;
import cn.itcast.hiss.api.client.task.ApproveTask;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.form.FormSubmitDataMessage;
import cn.itcast.hiss.message.sender.task.ApproveTaskMessage;
import cn.itcast.hiss.process.activiti.service.ActivitiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

/**
 * Task_DraftHandler
 *
 * @author: miukoo
 * @describe: 发起人重新填写表单后，发起
 * @date: 2022/12/28 10:10
 */
@Component
public class Task_SubmitHandler implements CmdHandler<ApproveTaskMessage> {

    @Autowired
    private ActivitiService activitiService;

    /**
     * 暂存任务
     *
     * @param params
     * @param messageContext
     */
    @Override
    public void invoke(Message params, MessageContext messageContext) {
        ApproveTask payload = (ApproveTask) params.getPalyload();
        if (StrUtil.isEmpty(payload.getOperatorName())) {
            payload.setOperatorName("提交");
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
        return HandlerIdClientEnum.TASK_SUBMIT_TASK.getId();
    }

}
