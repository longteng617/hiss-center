package cn.itcast.hiss.process.activiti.handler.task;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.form.FormSubmitData;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.form.FormSubmitDataMessage;
import cn.itcast.hiss.process.activiti.service.ActivitiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Task_DraftHandler
 *
 * @author: miukoo
 * @describe: 暂存预备任务的表单数据
 * @date: 2022/12/28 10:10
 */
@Component
public class Task_SendHandler implements CmdHandler<FormSubmitDataMessage> {

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
        FormSubmitData palyload = (FormSubmitData) params.getPalyload();
        if(StrUtil.isEmpty( palyload.getOperatorName())){
            palyload.setOperatorName("发起");
        }
        activitiService.sendPreProcess((FormSubmitDataMessage)params,messageContext);
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.TASK_SEND_TASK.getId();
    }

}
