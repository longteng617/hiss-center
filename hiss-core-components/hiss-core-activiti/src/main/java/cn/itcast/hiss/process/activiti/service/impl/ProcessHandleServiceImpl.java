package cn.itcast.hiss.process.activiti.service.impl;

import cn.itcast.hiss.common.dtos.PageResponseResult;
import cn.itcast.hiss.common.enums.ProcessStatusEnum;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sys.ProcessInstanceMessage;
import cn.itcast.hiss.message.sys.pojo.ProcessInstance;
import cn.itcast.hiss.process.activiti.mapper.ProcessApplyMapper;
import cn.itcast.hiss.process.activiti.mapper.ProcessHandleMapper;
import cn.itcast.hiss.process.activiti.service.ProcessHandleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/*
 * @author miukoo
 * @description 流程办理服务类
 * @date 2023/7/2 9:54
 * @version 1.0
 **/
@Service
public class ProcessHandleServiceImpl implements ProcessHandleService {
    @Autowired
    ProcessHandleMapper processHandleMapper;

    @Override
    public void listProcessHandle(ProcessInstanceMessage message, MessageContext messageContext) {
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
        if(processInstance.getStatus()== ProcessStatusEnum.ACTIVE){
            processInstances = processHandleMapper.activeStatusList(userId, userAppId,name,businessKey, page, size);
            total = processHandleMapper.activeStatusTotal(userId, userAppId,name,businessKey);
        }else if(processInstance.getStatus()== ProcessStatusEnum.COMPLETE){
            processInstances = processHandleMapper.complateStatusList(userId, userAppId,name,businessKey, page, size);
            total = processHandleMapper.complateStatusTotal(userId, userAppId,name,businessKey);
        }

        PageResponseResult pageResponseResult = new PageResponseResult(processInstance.getCurrent(),processInstance.getPageSize(),total);
        pageResponseResult.setData(processInstances);

        messageContext.addResultAndCount("result",pageResponseResult);
    }

}
