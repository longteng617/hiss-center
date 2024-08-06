package cn.itcast.hiss.process.activiti.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.HissProcessConstants;
import cn.itcast.hiss.common.dtos.PageResponseResult;
import cn.itcast.hiss.common.enums.ProcessStatusEnum;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sys.ProcessInstanceMessage;
import cn.itcast.hiss.message.sys.pojo.ProcessInstance;
import cn.itcast.hiss.process.activiti.mapper.HissProcessFormMapper;
import cn.itcast.hiss.process.activiti.mapper.HissProcessPreLaunchMapper;
import cn.itcast.hiss.process.activiti.mapper.ProcessApplyMapper;
import cn.itcast.hiss.process.activiti.mapper.ProcessInstanceMapper;
import cn.itcast.hiss.process.activiti.multilnstance.DeleteProcessInstanceCmd;
import cn.itcast.hiss.process.activiti.pojo.HissProcessForm;
import cn.itcast.hiss.process.activiti.pojo.HissProcessPreLaunch;
import cn.itcast.hiss.process.activiti.service.ProcessInstanceService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @author miukoo
 * @description 对流程实例进行管理
 * @date 2023/7/9 19:11
 * @version 1.0
 **/
@Service
public class ProcessInstanceServiceImpl implements ProcessInstanceService {
    @Autowired
    ProcessApplyMapper processApplyMapper;

    @Autowired
    HissProcessPreLaunchMapper hissProcessPreLaunchMapper;

    @Autowired
    HissProcessFormMapper hissProcessFormMapper;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    @Autowired
    HistoryService historyService;

    @Autowired
    ManagementService managementService;
    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Override
    public void listProcessInstance(ProcessInstanceMessage message, MessageContext messageContext) {
        ProcessInstance processInstance = message.getPalyload();
        processInstance.checkParam();
        Long page = (processInstance.getCurrent()-1)*processInstance.getPageSize();
        Long size = processInstance.getPageSize();
        String userAppId = processInstance.getTenantId();
        String name = processInstance.getName();
        String businessKey = processInstance.getBusinessKey();
        List<ProcessInstance> processInstances = new ArrayList<>();//返回的列表
        long total = 0;// 返回的总数
        if(processInstance.getStatus()== ProcessStatusEnum.CANCEL){
            processInstances = processInstanceMapper.cancelStatusList( userAppId,name,businessKey, page, size);
            total = processInstanceMapper.cancelStatusTotal(userAppId,name,businessKey);
        }else if(processInstance.getStatus()== ProcessStatusEnum.ACTIVE){
            processInstances = processInstanceMapper.activeStatusList( userAppId,name,businessKey, page, size);
            total = processInstanceMapper.activeStatusTotal(userAppId,name,businessKey);
        }else if(processInstance.getStatus()== ProcessStatusEnum.COMPLETE){
            processInstances = processInstanceMapper.complateStatusList(userAppId,name,businessKey, page, size);
            total = processInstanceMapper.complateStatusTotal( userAppId,name,businessKey);
        }else if(processInstance.getStatus()== ProcessStatusEnum.PREPARE){
            processInstances = processInstanceMapper.prepareStatusList( userAppId,name,businessKey, page, size);
            total = processInstanceMapper.prepareStatusTotal( userAppId,name,businessKey);
        }else{
            processInstances = processInstanceMapper.allStatusList(userAppId,name,businessKey, page, size);
            total = processInstanceMapper.allStatusTotal(userAppId,name,businessKey);
        }

        PageResponseResult pageResponseResult = new PageResponseResult(processInstance.getCurrent(),processInstance.getPageSize(),total);
        pageResponseResult.setData(processInstances);

        messageContext.addResultAndCount("result",pageResponseResult);
    }

    /**
     * 删除预备流程和取消的流程
     * @param message
     * @param messageContext
     */
    @Override
    public void deleteProcessInstance(ProcessInstanceMessage message, MessageContext messageContext) {
        ProcessInstance palyload = message.getPalyload();
        if(StrUtil.isNotEmpty(palyload.getId())){
            HissProcessPreLaunch launch = hissProcessPreLaunchMapper.selectById(palyload.getId());
            if(launch!=null){
                // 是预备流程删除
                hissProcessFormMapper.delete(Wrappers.<HissProcessForm>lambdaQuery().eq(HissProcessForm::getLaunchId,launch.getId()));
                hissProcessPreLaunchMapper.deleteById(launch.getId());
                messageContext.addResultAndCount("msg","删除成功！");
            }else{
                // 流程实例删除
                deleteHiProcessInstance(messageContext, palyload);
            }
        }else{
            messageContext.addError("msg","未找到要删除的数据");
        }
    }

    /**
     * 删除历史取消的流程
     * @param messageContext
     * @param palyload
     */
    public void deleteHiProcessInstance(MessageContext messageContext, ProcessInstance palyload) {
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(palyload.getId()).singleResult();
        if(historicProcessInstance!=null){
            List<HistoricVariableInstance> list = historyService.createHistoricVariableInstanceQuery().processInstanceId(palyload.getId()).list();
            Map<String, Object> processVariables = new HashMap<>();
            if(list!=null){
                for (HistoricVariableInstance historicVariableInstance : list) {
                    processVariables.put(historicVariableInstance.getVariableName(),historicVariableInstance.getValue());
                }
            }
            if(processVariables!=null){
                String status = (String) processVariables.get(HissProcessConstants.PROCESS_STATUS);
                if(ProcessStatusEnum.CANCEL.name().equals(status)){
                    managementService.executeCommand(new DeleteProcessInstanceCmd(historicProcessInstance.getId()));
                    messageContext.addResultAndCount("msg","删除成功！");
                }else{
                    messageContext.addError("msg","只能删除【取消/待发起】了的流程");
                }
            }else{
                messageContext.addError("msg","只能删除【取消/待发起】状态的流程");
            }
        }else{
            messageContext.addError("msg","为找到对应的流程实例数据");
        }
    }
}
