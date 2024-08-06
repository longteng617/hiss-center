package cn.itcast.hiss.process.activiti.handler.processinstance;

import cn.itcast.hiss.api.client.processinstance.HissProcessInstance;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.processinstance.HissProcessInstanceMessage;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.builders.StartProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * StartProcess
 *
 * @author: wgl
 * @describe: 启动流程实例
 * @date: 2022/12/28 10:10
 */
@Component
public class PI_StartProcessHandler implements CmdHandler<HissProcessInstanceMessage> {

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        HissProcessInstance palyload = (HissProcessInstance) params.getPalyload();
        StartProcessPayloadBuilder startProcessPayloadBuilder = ProcessPayloadBuilder
                .start()
                .withProcessDefinitionId(palyload.getId())
                .withName(palyload.getName())
                .withVariables(palyload.getVariables());
        //设置系统内置的流程变量
        StartProcessPayload build = startProcessPayloadBuilder.build();
        messageContext.setResult(new ConcurrentHashMap<>() {
            {
                put("data", build);
            }
        });
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.PI_START_PROCESS.getId();
    }
}
