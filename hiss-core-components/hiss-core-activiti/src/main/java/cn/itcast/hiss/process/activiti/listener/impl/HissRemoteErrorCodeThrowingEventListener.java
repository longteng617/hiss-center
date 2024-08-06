package cn.itcast.hiss.process.activiti.listener.impl;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.common.enums.EventOperationTypeEnum;
import cn.itcast.hiss.event.pojo.HissActivitiEvent;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.process.activiti.listener.HissActivitiEventBuilder;
import cn.itcast.hiss.server.template.HissServerApperanceTemplate;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.impl.bpmn.helper.ErrorThrowingEventListener;
import org.activiti.engine.impl.bpmn.helper.MessageThrowingEventListener;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/*
 * @author miukoo
 * @description 抛出错误码等信息
 * @date 2023/5/28 10:18
 * @version 1.0
 **/
@Component
@Scope("prototype")
@Slf4j
@Data
public class HissRemoteErrorCodeThrowingEventListener extends ErrorThrowingEventListener {
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    HissServerApperanceTemplate hissServerApperanceTemplate;
    boolean failOnException = false;
    @Autowired
    RuntimeService runtimeService;
    @Override
    public void onEvent(ActivitiEvent event) {
        if (isValidEvent(event)) {
            super.onEvent(event);
            HissActivitiEvent hissActivitiEvent = HissActivitiEventBuilder.builder()
                    .eventName(event.getType().name())
                    .targetName(errorCode)
                    .activitiEvent(event)
                    .build();
            hissActivitiEvent.setOperationType(EventOperationTypeEnum.ERROR_CODE_EVENT_NOTICE);
            ProcessDefinition processDefinition = repositoryService.getProcessDefinition(event.getProcessDefinitionId());
            if(StrUtil.isNotEmpty(event.getProcessInstanceId())){
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(event.getProcessInstanceId()).singleResult();
                if(processInstance!=null){
                    hissActivitiEvent.setBusinessKey(processInstance.getBusinessKey());
                    hissActivitiEvent.setVariables(processInstance.getProcessVariables());
                }
            }
            notifyClient(processDefinition.getTenantId(), hissActivitiEvent);
        }
    }

    public void notifyClient(String tenantId,HissActivitiEvent hissActivitiEvent) {
        MessageContext messageContext = hissServerApperanceTemplate.eventActivitiProcessNotice(tenantId, hissActivitiEvent);
        log.trace("===========>:" + JSON.toJSONString(messageContext));
        failOnException = !messageContext.isSuccess();
    }

    @Override
    public boolean isFailOnException() {
        return failOnException;
    }

}
