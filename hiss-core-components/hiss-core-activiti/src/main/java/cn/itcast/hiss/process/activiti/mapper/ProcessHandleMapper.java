package cn.itcast.hiss.process.activiti.mapper;

import cn.itcast.hiss.message.sys.pojo.ProcessInstance;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/*
 * @author miukoo
 * @description 查询流程办理中的数据mapper
 * @date 2023/6/30 20:54
 * @version 1.0
 **/
public interface ProcessHandleMapper {

    /**
     * 待办状态的数据
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
     * 已办状态的数据
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


}
