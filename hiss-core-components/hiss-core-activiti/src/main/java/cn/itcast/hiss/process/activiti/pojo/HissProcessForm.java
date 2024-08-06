package cn.itcast.hiss.process.activiti.pojo;
import cn.itcast.hiss.api.client.form.FormDefinitionInfo;
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
import java.util.List;

/**
* <p>
* hiss_process_form 实体类
* </p>
*
* @author lenovo
* @since 2023-06-21 08:31:26
*/
@Getter
@Setter
@TableName("hiss_process_form")
public class HissProcessForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
    * 主键
    */
    @TableId(type =IdType.ASSIGN_UUID)
    private String id;

    /**
    * 创建时间
    */
    private Date createdTime;

    /**
    * 流程实例ID
    */
    private String processInstanceId;

    /**
    * 表单ID
    */
    private String formId;

    /**
     * 节点ID
     */
    private String nodeId;

    /**
    * 表单类型
    */
    private String formType;

    /**
     * 表单名称
     */
    private String formName;

    /**
    * 主表数据ID
    */
    private String dataId;

    /**
     * 租户
     */
    private String tenantId;

    /**
     * 预备流程ID
     */
    private String launchId;

    @TableField(exist = false)
    private List<FormDefinitionInfo> fields;


}
