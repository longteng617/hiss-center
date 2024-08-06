package cn.itcast.hiss.form.mapper;

import cn.itcast.hiss.form.pojo.HissFormTables;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* <p>
* hiss_form_tables Mapper 接口
* </p>
*
* @author lenovo
* @since 2023-06-26 14:25:21
*/
public interface HissFormTablesMapper extends BaseMapper<HissFormTables> {

    @Select("select * from hiss_form_tables where model_id=#{modelId}")
    public List<HissFormTables> listByModelId(@Param("modelId") String modelId);

    @Select("select * from hiss_form_tables where parent_id=#{parentId} and  field_id=#{fieldId} limit 1")
    public HissFormTables selectSubTableByParentIdAndFieldId(@Param("parentId") String parentId,@Param("fieldId") String fieldId);

    @Delete("delete from hiss_form_tables where model_id=#{modelId}")
    public void deleteByModelId(@Param("modelId") String modelId);

}
