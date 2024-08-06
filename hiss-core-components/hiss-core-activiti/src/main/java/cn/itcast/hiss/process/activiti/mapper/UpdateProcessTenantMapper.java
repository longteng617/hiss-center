package cn.itcast.hiss.process.activiti.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/*
 * @author miukoo
 * @description 动态修改一些基本信息（知会、抄送使用）
 * @date 2023/7/9 10:04
 * @version 1.0
 **/
public interface UpdateProcessTenantMapper {


    @Update("update act_ru_execution set tenant_id_=#{tenantId},name_=#{name} where proc_inst_id_=#{processInstanceId}")
    public void updateExecution(@Param("processInstanceId") String processInstanceId,
                                @Param("tenantId") String tenantId,
                                @Param("name") String name);

    @Update("update act_ru_task set tenant_id_=#{tenantId} where proc_inst_id_=#{processInstanceId}")
    public void updateTask(@Param("processInstanceId") String processInstanceId,
                                @Param("tenantId") String tenantId);

    @Update("update act_hi_procinst set tenant_id_=#{tenantId},name_=#{name} where proc_inst_id_=#{processInstanceId}")
    public void updateHiProcinst(@Param("processInstanceId") String processInstanceId,
                                @Param("tenantId") String tenantId,
                                @Param("name") String name);

    @Select("select count(1) from  act_hi_procinst where proc_inst_id_=#{processInstanceId}")
    public int countHiProcinst(@Param("processInstanceId") String processInstanceId);

    @Update("update act_hi_actinst set tenant_id_=#{tenantId} where proc_inst_id_=#{processInstanceId}")
    public void updateHiActinst(@Param("processInstanceId") String processInstanceId,
                           @Param("tenantId") String tenantId);

    @Update("update act_hi_taskinst set tenant_id_=#{tenantId} where proc_inst_id_=#{processInstanceId}")
    public void updateHiTaskinst(@Param("processInstanceId") String processInstanceId,
                                @Param("tenantId") String tenantId);

}
