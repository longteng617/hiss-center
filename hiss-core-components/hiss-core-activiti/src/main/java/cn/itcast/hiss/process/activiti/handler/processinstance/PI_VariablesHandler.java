package cn.itcast.hiss.process.activiti.handler.processinstance;

import cn.hutool.core.bean.BeanUtil;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.processinstance.HissProcessInstanceMessage;
import cn.itcast.hiss.process.activiti.dto.processinstance.VariableInstanceDTO;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

;

/**
 * ProcessInstanceVariables
 *
 * @author: wgl
 * @describe: 流程实例变量
 * @date: 2022/12/28 10:10
 */

@Component
public class PI_VariablesHandler implements CmdHandler<HissProcessInstanceMessage> {

    @Autowired
    private ProcessRuntime processRuntime;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        HissProcessInstanceMessage palyload = (HissProcessInstanceMessage) params.getPalyload();
        List<VariableInstance> variableInstance = processRuntime.variables(ProcessPayloadBuilder
                .variables()
                .withProcessInstanceId(palyload.getId())
                .build());
        List<VariableInstanceDTO> res = new ArrayList<>();
        for (VariableInstance index : variableInstance) {
            VariableInstanceDTO variableInstanceDTO = new VariableInstanceDTO();
            BeanUtil.copyProperties(index, variableInstanceDTO);
            res.add(variableInstanceDTO);
        }
        messageContext.setResult(new ConcurrentHashMap<>() {
            {
                put("variable", res);
            }
        });
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.PI_VARIABLES.getId();
    }
}
