package cn.itcast.hiss.process.activiti.service;

import org.apache.ibatis.annotations.Param;

/*
 * @author miukoo
 * @description 更新知会、抄送的归属信息
 * @date 2023/7/9 10:17
 * @version 1.0
 **/
public interface UpdateProcessTenantService {

    public void updateTanantAndName(String processInstanceId,String tenantId,String name );

}
