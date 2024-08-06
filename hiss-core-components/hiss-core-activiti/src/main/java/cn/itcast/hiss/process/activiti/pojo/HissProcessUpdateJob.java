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
* hiss_process_update_job 实体类
* </p>
*
* @author lenovo
* @since 2023-07-12 14:01:39
*/
@Getter
@Setter
@TableName("hiss_process_update_job")
public class HissProcessUpdateJob implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
    * 主键
    */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
    * 创建时间
    */
    private Date createdTime;

    /**
    * 执行时间
    */
    private Date executedTime;

    /**
    * 更新名称
    */
    private String name;

    /**
     * 更新次数
     */
    private int count;

    /**
     * 执行时间间隔
     */
    private int delay;

    /**
    * 流程ID
    */
    private String processInstanceId;

    /**
    * 业务号
    */
    private String tenantId;


}
