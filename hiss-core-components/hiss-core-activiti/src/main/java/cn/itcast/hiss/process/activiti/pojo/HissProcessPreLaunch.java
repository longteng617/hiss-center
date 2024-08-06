package cn.itcast.hiss.process.activiti.pojo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
* <p>
* hiss_process_pre_launch 实体类
* </p>
*
* @author lenovo
* @since 2023-06-14 11:35:20
*/
@Getter
@Setter
@TableName("hiss_process_pre_launch")
public class HissProcessPreLaunch implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
    * 主键
    */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
    * 业务ID
    */
    private String businessKey;

    /**
    * 创建时间
    */
    private Date createdTime;

    /**
    * 流程模型ID
    */
    private String modelId;

    /**
    * 流程模型名称
    */
    private String modelName;

    /**
    * 租户ID
    */
    private String tenantId;

    /**
    * 用户ID
    */
    private String userId;

    /**
    * 用户名
    */
    private String userName;


}
