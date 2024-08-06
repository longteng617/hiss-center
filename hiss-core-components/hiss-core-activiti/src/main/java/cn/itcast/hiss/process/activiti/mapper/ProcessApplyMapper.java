package cn.itcast.hiss.process.activiti.mapper;

import cn.itcast.hiss.message.sys.pojo.ProcessInstance;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;

/*
 * @author miukoo
 * @description 查询流程多表数据的mapper
 * @date 2023/6/30 20:54
 * @version 1.0
 **/
public interface ProcessApplyMapper {

    /**
     * 用户申请中的数据
     * @param userId
     * @param userAppId
     * @param page
     * @param size
     * @return
     */
    public List<ProcessInstance> activeStatusList(@Param("startUserId") String userId, @Param("tenantId") String userAppId,
                                                  @Param("name") String name,@Param("businessKey") String businessKey,
                                                  @Param("page") Long page, @Param("size") Long size);
    public long activeStatusTotal(@Param("startUserId") String userId, @Param("tenantId") String userAppId,
                                  @Param("name") String name,@Param("businessKey") String businessKey);

    /**
     * 用户完成的数据：不包括取消的
     * @param userId
     * @param userAppId
     * @param page
     * @param size
     * @return
     */
    public List<ProcessInstance> complateStatusList(
            @Param("startUserId") String userId, @Param("tenantId") String userAppId,
            @Param("name") String name,@Param("businessKey") String businessKey,
            @Param("page") Long page, @Param("size") Long size);
    public long complateStatusTotal(
            @Param("startUserId") String userId, @Param("tenantId") String userAppId,
            @Param("name") String name,@Param("businessKey") String businessKey);

    /**
     * 用户完成的数据：有删除原因的数据
     * @param userId
     * @param userAppId
     * @param page
     * @param size
     * @return
     */
    public List<ProcessInstance> cancelStatusList(
            @Param("startUserId") String userId, @Param("tenantId") String userAppId,
            @Param("name") String name,@Param("businessKey") String businessKey,
            @Param("page") Long page, @Param("size") Long size);

    public long cancelStatusTotal(@Param("startUserId") String userId, @Param("tenantId") String userAppId,
                                  @Param("name") String name,@Param("businessKey") String businessKey);

    /**
     * 用户预发起的任务：在hiss_process_pre_launch表中存储着
     * @param userId
     * @param userAppId
     * @param page
     * @param size
     * @return
     */
    public List<ProcessInstance> prepareStatusList(
            @Param("startUserId") String userId, @Param("tenantId") String userAppId,
            @Param("name") String name,@Param("businessKey") String businessKey,
            @Param("page") Long page, @Param("size") Long size);

    public long prepareStatusTotal(
            @Param("startUserId") String userId, @Param("tenantId") String userAppId,
            @Param("name") String name,@Param("businessKey") String businessKey);

    /**
     * 用户发起的全部数据
     * @param userId
     * @param userAppId
     * @param page
     * @param size
     * @return
     */
    public List<ProcessInstance> allStatusList(
            @Param("startUserId") String userId, @Param("tenantId") String userAppId,
            @Param("name") String name,@Param("businessKey") String businessKey,
            @Param("page") Long page, @Param("size") Long size);

    public long allStatusTotal(
            @Param("startUserId") String userId, @Param("tenantId") String userAppId,
            @Param("name") String name,@Param("businessKey") String businessKey);

}
