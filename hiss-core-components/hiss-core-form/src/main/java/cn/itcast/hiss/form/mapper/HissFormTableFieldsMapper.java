package cn.itcast.hiss.form.mapper;

import cn.itcast.hiss.api.client.form.FormDefinitionInfo;
import cn.itcast.hiss.form.pojo.HissFormTableFields;
import cn.itcast.hiss.form.pojo.HissFormTables;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* <p>
* hiss_form_table_fields Mapper 接口
* </p>
*
* @author lenovo
* @since 2023-06-26 14:25:21
*/
public interface HissFormTableFieldsMapper extends BaseMapper<HissFormTableFields> {

    @Select("select * from hiss_form_table_fields where table_id=#{tableId}")
    public List<HissFormTableFields> listByTableId(@Param("tableId") String tableId);

    @Select("select control_id as fieldName, field_name as fieldLabel  from hiss_form_table_fields where model_id=#{modelId} and level=0")
    public List<FormDefinitionInfo> listByModelIdForProcessConfig(@Param("modelId") String modelId);

    @Select("select * from hiss_form_table_fields where model_id=#{modelId}")
    public List<HissFormTableFields> listByModelId(@Param("modelId") String modelId);

    @Select("select * from hiss_form_table_fields where model_id=#{modelId} and control_id=#{controlId}")
    public HissFormTableFields getByModelIdAndFieldId(@Param("modelId") String modelId, @Param("controlId") String controlId);

    @Delete("delete from hiss_form_table_fields where model_id=#{modelId}")
    public void deleteByModelId(@Param("modelId") String modelId);

}
