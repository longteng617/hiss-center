package cn.itcast.hiss.process.activiti.multilnstance;

import cn.itcast.hiss.process.activiti.service.UpdateProcessTenantService;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.ExecutionQueryImpl;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.*;
import org.activiti.engine.runtime.ProcessInstance;

import java.io.Serializable;
import java.util.List;

/*
 * @author miukoo
 * @description 修改抄送和知会的租户ID
 * @date 2023/7/2 17:50
 * @version 1.0
 **/
public class SetProcessInstanceTenantIdCmd implements Command<Void>, Serializable {

    private static final long serialVersionUID = 1L;

    protected String processInstanceId;
    protected String tenantId;
    protected String name;
    protected UpdateProcessTenantService updateProcessTenantService;

    public SetProcessInstanceTenantIdCmd(UpdateProcessTenantService updateProcessTenantService,String processInstanceId, String name,String tenantId) {
        this.processInstanceId = processInstanceId;
        this.tenantId = tenantId;
        this.name = name;
        this.updateProcessTenantService = updateProcessTenantService;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        if (processInstanceId == null) {
            throw new ActivitiIllegalArgumentException("processInstanceId is null");
        }
        System.out.println("===============更新内容："+processInstanceId+"\t: "+tenantId+"\t: "+name);

        updateProcessTenantService.updateTanantAndName(processInstanceId,tenantId,name);

        return null;
    }

}
