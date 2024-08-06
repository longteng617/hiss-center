package cn.itcast.hiss.process.activiti.handler.processinstance;

import cn.itcast.hiss.api.client.common.PageInfo;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.common.PageInfoMessage;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * GetInstance
 *
 * @author: wgl
 * @describe: 获取流程实例
 * @date: 2022/12/28 10:10
 */
@Component
public class PI_GetInstanceHandler implements CmdHandler<PageInfoMessage> {

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        PageInfo palyload = (PageInfo) params.getPalyload();
        // 设置查询条件
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
                .processInstanceTenantId(params.getMessageAuth().getTenant()) // 租户ID
                .orderByProcessInstanceId().desc(); // 按照流程实例ID降序排序
        // 获取分页数据
        List<ProcessInstance> processInstances = query.listPage(palyload.getPageNum(), palyload.getPageSize());
        List<HashMap<String, Object>> listMap = new ArrayList<HashMap<String, Object>>();
        for (ProcessInstance pi : processInstances) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("id", pi.getId());
            hashMap.put("name", pi.getName());
            hashMap.put("processDefinitionId", pi.getProcessDefinitionId());
            hashMap.put("processDefinitionKey", pi.getProcessDefinitionKey());
            hashMap.put("startTime", pi.getStartTime());
            hashMap.put("processDefinitionVersion", pi.getProcessDefinitionVersion());
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(pi.getProcessDefinitionId())
                    .singleResult();
            hashMap.put("resourceName", pd.getResourceName());
            hashMap.put("deploymentId", pd.getDeploymentId());
            listMap.add(hashMap);
        }
        messageContext.setResult(new ConcurrentHashMap<>() {
            {
                put("data", listMap);
                put("total", query.count());
            }
        });

    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.PI_GET_INSTANCE.getId();
    }
}
