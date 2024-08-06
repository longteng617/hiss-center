package cn.itcast.hiss.process.activiti.handler.processinstance;

import cn.hutool.core.util.ObjectUtil;
import cn.itcast.hiss.api.client.common.HissVariable;
import cn.itcast.hiss.api.client.common.HissVariableServer;
import cn.itcast.hiss.api.client.processinstance.CreateProcessInstance;
import cn.itcast.hiss.api.client.processinstance.HissProcessInstance;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.common.SystemConstant;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.processinstance.CreateProcessInstanceMessage;
import cn.itcast.hiss.process.activiti.variables.SysVariableManager;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * CreateInstanceHandler
 *
 * @author: wgl
 * @describe: 创建流程实例
 * @date: 2022/12/28 10:10
 */
@Component
public class PI_CreateInstanceHandler implements CmdHandler<CreateProcessInstanceMessage> {

    @Autowired
    private RuntimeService runtimeService;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        List<HissVariableServer> variable = SysVariableManager.getTenantVariable(params.getMessageAuth().getTenant());
        List<String> keyList = variable.stream().map(HissVariableServer::getKey).collect(Collectors.toList());
        CreateProcessInstance palyload = (CreateProcessInstance) params.getPalyload();
        Map<String, Object> variables = new HashMap<>();
        //处理客户端提交的流程变量
        if (ObjectUtil.isNotNull(palyload.getVariables())) {
            for (String indexKey : palyload.getVariables().keySet()) {
                //判断是否是客户端上报的变量
                if (keyList.contains(indexKey)) {
                    //说明是客户端上报的变量--做特殊处理  系统标识/客户端上报Key/用户填写的Value
                    variables.put(indexKey, SystemConstant.CLIENT_FLAG + "/" + palyload.getVariables().get(indexKey) + (ObjectUtil.isNotNull(palyload.getVariables().get(indexKey)) ? "/" + palyload.getVariables().get(indexKey).toString() : ""));

                } else {
                    //不是客户端上报的变量--直接存储
                    variables.put(indexKey, palyload.getVariables().get(indexKey));
                }
            }
        }
        // 设置流程发起人信息
        variables.put(SystemConstant.TASK_VARIABLES_CREATE_USERID, params.getMessageAuth().getCurrentUser().getUserId());
        variables.put(SystemConstant.TASK_VARIABLES_CREATE_USERNAME, params.getMessageAuth().getCurrentUser().getUserName());
        //获取客户端上报的的所有流程变量
        for (HissVariableServer index : variable) {
            variables.put(index.getKey(), SystemConstant.CLIENT_FLAG + "/" + index.getValue());
        }
        //封装完成任务数量
        ProcessInstance myDeployment = runtimeService.startProcessInstanceById(palyload.getProcessDefinitionId(), variables);
//        ProcessInstance myDeployment = runtimeService.startProcessInstanceById(buildId(palyload), variables);
        messageContext.setResult(new ConcurrentHashMap<>() {
            {
                put("result", myDeployment);
            }
        });
    }

    /**
     * 根据载荷中的数据构建消息
     *
     * @param palyload
     * @return
     */
    private String buildId(HissProcessInstance palyload) {
        return palyload.getKey() + ":" + palyload.getVersion() + ":" + palyload.getId();
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.PI_CREATE_INSTANCE.getId();
    }
}
