package cn.itcast.hiss.process.activiti.service.impl;

import cn.itcast.hiss.process.activiti.mapper.UpdateProcessTenantMapper;
import cn.itcast.hiss.process.activiti.service.UpdateProcessTenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * @author miukoo
 * @description
 * @date 2023/7/9 10:20
 * @version 1.0
 **/
@Service
@Transactional
public class UpdateProcessTenantServiceImpl implements UpdateProcessTenantService {

    @Autowired
    UpdateProcessTenantMapper updateProcessTenantMapper;

    @Override
    public void updateTanantAndName(String processInstanceId, String tenantId, String name) {
        updateProcessTenantMapper.updateExecution(processInstanceId,tenantId,name);
        updateProcessTenantMapper.updateHiProcinst(processInstanceId,tenantId,name);
        updateProcessTenantMapper.updateHiActinst(processInstanceId,tenantId);
        updateProcessTenantMapper.updateHiTaskinst(processInstanceId,tenantId);
        updateProcessTenantMapper.updateTask(processInstanceId,tenantId);
    }

}
