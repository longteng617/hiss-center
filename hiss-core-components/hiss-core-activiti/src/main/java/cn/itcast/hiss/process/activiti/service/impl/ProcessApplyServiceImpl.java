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
import cn.itcast.hiss.process.activiti.multilnstance.DeleteProcessInstanceCmd;
import cn.itcast.hiss.process.activiti.pojo.HissProcessForm;
import cn.itcast.hiss.process.activiti.pojo.HissProcessPreLaunch;
import cn.itcast.hiss.process.activiti.service.ProcessApplyService;
import cn.itcast.hiss.process.activiti.service.ProcessInstanceService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * @author miukoo
 * @description 查询个人的发起的申请流程
 * @date 2023/6/30 20:51
 * @version 1.0
 **/
@Service
@Transactional
public class ProcessApplyServiceImpl implements ProcessApplyService {

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
    ProcessInstanceService processInstanceService;

    @Override
    public void listProcessApply(ProcessInstanceMessage message, MessageContext messageContext) {
        ProcessInstance processInstance = message.getPalyload();
        processInstance.checkParam();
        Long page = (processInstance.getCurrent()-1)*processInstance.getPageSize();
        Long size = processInstance.getPageSize();
        String userId = message.getMessageAuth().getCurrentUser().getUserId();
        String userAppId = message.getMessageAuth().getTenant();
        String name = processInstance.getName();
        String businessKey = processInstance.getBusinessKey();
        List<ProcessInstance> processInstances = new ArrayList<>();//返回的列表
        long total = 0;// 返回的总数
        if(processInstance.getStatus()== ProcessStatusEnum.CANCEL){
            processInstances = processApplyMapper.cancelStatusList(userId, userAppId,name,businessKey, page, size);
            total = processApplyMapper.cancelStatusTotal(userId, userAppId,name,businessKey);
        }else if(processInstance.getStatus()== ProcessStatusEnum.ACTIVE){
            processInstances = processApplyMapper.activeStatusList(userId, userAppId,name,businessKey, page, size);
            total = processApplyMapper.activeStatusTotal(userId, userAppId,name,businessKey);
        }else if(processInstance.getStatus()== ProcessStatusEnum.COMPLETE){
            processInstances = processApplyMapper.complateStatusList(userId, userAppId,name,businessKey, page, size);
            total = processApplyMapper.complateStatusTotal(userId, userAppId,name,businessKey);
        }else if(processInstance.getStatus()== ProcessStatusEnum.PREPARE){
            processInstances = processApplyMapper.prepareStatusList(userId, userAppId,name,businessKey, page, size);
            total = processApplyMapper.prepareStatusTotal(userId, userAppId,name,businessKey);
        }else{
            processInstances = processApplyMapper.allStatusList(userId, userAppId,name,businessKey, page, size);
            total = processApplyMapper.allStatusTotal(userId, userAppId,name,businessKey);
        }

        PageResponseResult pageResponseResult = new PageResponseResult(processInstance.getCurrent(),processInstance.getPageSize(),total);
        pageResponseResult.setData(processInstances);

        messageContext.addResultAndCount("result",pageResponseResult);
    }

    /**
     * 删除个人申请的流程
     * @param message
     * @param messageContext
     */
    @Override
    public void deleteProcessApply(ProcessInstanceMessage message, MessageContext messageContext) {
        ProcessInstance palyload = message.getPalyload();
        if(StrUtil.isNotEmpty(palyload.getId())){
            HissProcessPreLaunch launch = hissProcessPreLaunchMapper.selectById(palyload.getId());
            String userId = message.getMessageAuth().getCurrentUser().getUserId();
            if(launch!=null){
                // 是预备流程删除
                if(userId.equals(launch.getUserId())){
                    hissProcessFormMapper.delete(Wrappers.<HissProcessForm>lambdaQuery().eq(HissProcessForm::getLaunchId,launch.getId()));
                    hissProcessPreLaunchMapper.deleteById(launch.getId());
                    messageContext.addResultAndCount("msg","删除成功！");
                }else{
                    messageContext.addError("msg","无权限进行此操作");
                }
            }else{
                // 流程实例删除
                processInstanceService.deleteHiProcessInstance(messageContext, palyload);
            }
        }else{
            messageContext.addError("msg","未找到要删除的数据");
        }
    }

}
