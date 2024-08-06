package cn.itcast.hiss.process.activiti.service.impl;

import cn.itcast.hiss.process.activiti.mapper.HissProcessUpdateJobMapper;
import cn.itcast.hiss.process.activiti.mapper.UpdateProcessTenantMapper;
import cn.itcast.hiss.process.activiti.multilnstance.SetProcessInstanceTenantIdCmd;
import cn.itcast.hiss.process.activiti.pojo.HissProcessUpdateJob;
import cn.itcast.hiss.process.activiti.service.UpdateProcessTenantService;
import org.activiti.engine.ManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/*
 * @author miukoo
 * @description 延迟更新流程实例基本信息
 * @date 2023/7/12 14:13
 * @version 1.0
 *
 **/
@Service
public class HissProcessUpdateJobServiceImpl {
    @Autowired
    HissProcessUpdateJobMapper hissProcessUpdateJobMapper;

    @Autowired
    ManagementService managementService;

    @Autowired
    private UpdateProcessTenantService updateProcessTenantService;

    @Autowired
    UpdateProcessTenantMapper updateProcessTenantMapper;

    @Scheduled(initialDelay = 10000,fixedRate = 5000)
    public void updateJob(){
        List<HissProcessUpdateJob> list = hissProcessUpdateJobMapper.listJob();
        if(list!=null){
            for (HissProcessUpdateJob hissProcessUpdateJob : list) {
                // 如果任务执行时，流程数据还未持久化到数据库中，则尝试3次,第3次会强制更新
                int count = updateProcessTenantMapper.countHiProcinst(hissProcessUpdateJob.getProcessInstanceId());
                if(count>0 || hissProcessUpdateJob.getCount()>=2) {
                    managementService.executeCommand(new SetProcessInstanceTenantIdCmd(updateProcessTenantService,
                            hissProcessUpdateJob.getProcessInstanceId(), hissProcessUpdateJob.getName(), hissProcessUpdateJob.getTenantId()));
                    hissProcessUpdateJobMapper.deleteById(hissProcessUpdateJob.getId());
                }else{
                    hissProcessUpdateJob.setCount(hissProcessUpdateJob.getCount()+1);
                    long nextTime = System.currentTimeMillis()+hissProcessUpdateJob.getDelay()*(hissProcessUpdateJob.getCount()+1);
                    hissProcessUpdateJob.setExecutedTime(new Date(nextTime));
                    hissProcessUpdateJobMapper.updateById(hissProcessUpdateJob);
                }
            }
        }
    }

}
