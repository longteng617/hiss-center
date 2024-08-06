package cn.itcast.hiss.process.activiti.mapper;

import cn.itcast.hiss.process.activiti.pojo.HissProcessForm;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* <p>
* hiss_process_form Mapper 接口
* </p>
*
* @author lenovo
* @since 2023-06-21 08:31:45
*/
public interface HissProcessFormMapper extends BaseMapper<HissProcessForm> {

    @Update("update hiss_process_form set data_id=#{dataId} where (launch_id=#{launchId} or process_instance_id=#{launchId}) and form_id=#{formId}")
    public void updateDataIdBy(@Param("formId") String formId,
                               @Param("dataId") String dataId,
                               @Param("launchId") String launchId);

    @Update("update hiss_process_form set process_instance_id=#{processInstanceId} where launch_id=#{launchId} and tenant_id=#{tenantId}")
    int updateLaunchToProcessInstance( @Param("launchId")String launchId, @Param("processInstanceId") String processInstanceId,@Param("tenantId") String tenantId);

    @Select("select count(1) from hiss_process_form  where form_id=#{formId} and tenant_id=#{tenantId}")
    int countFormId( @Param("formId")String formId,@Param("tenantId") String tenantId);

    @Select("select count(1) from hiss_process_form  where (launch_id=#{launchId} or process_instance_id=#{launchId}) and form_id=#{formId}")
    int countLaunchAndFormId( @Param("launchId")String launchId,@Param("formId") String formId);

    @Select("select form_id from hiss_process_form  where launch_id=#{launchId} and data_id=#{dataId}")
    String getFormIdByLaunchIdAndDataId(@Param("launchId")String launchId,@Param("dataId")  String dataId);

    @Select("select * from hiss_process_form  where launch_id=#{launchId}")
    List<HissProcessForm> listFormByLaunchId(@Param("launchId")String launchId);

    @Select("select * from hiss_process_form  where process_instance_id=#{processInstanceId}")
    List<HissProcessForm> listFormByProcessInstanceId(@Param("processInstanceId")String processInstanceId);

    @Select("select count(1) from hiss_process_form  where data_id=#{dataId} limit 1")
    int countByDataId(@Param("dataId")String dataId);

}
