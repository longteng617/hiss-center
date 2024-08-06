package cn.itcast.hiss.process.activiti.handler.processinstance;

import cn.itcast.hiss.api.client.processinstance.HissProcessInstance;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.processinstance.HissProcessInstanceMessage;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;


/**
 * ResumeInstance
 *
 * @author: wgl
 * @describe: 激活流程实例
 * @date: 2022/12/28 10:10
 */
@Component
public class PI_ResumeInstanceHandler implements CmdHandler<HissProcessInstanceMessage> {

    @Autowired
    private ProcessRuntime processRuntime;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        HissProcessInstance palyload = (HissProcessInstance) params.getPalyload();
        ProcessInstance processInstance = processRuntime.resume(ProcessPayloadBuilder
                .resume()
                .withProcessInstanceId(palyload.getId())
                .build()
        );
        messageContext.setResult(new ConcurrentHashMap<>() {
            {
                put("msg", "success");
                put("processInstance", processInstance.getId());
            }
        });
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.PI_RESUME_INSTANCE.getId();
    }
}
