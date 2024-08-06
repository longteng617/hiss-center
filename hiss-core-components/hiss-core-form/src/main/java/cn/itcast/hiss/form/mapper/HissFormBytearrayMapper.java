package cn.itcast.hiss.form.mapper;

import cn.itcast.hiss.form.pojo.HissFormBytearray;
import cn.itcast.hiss.form.pojo.HissFormTableFields;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* <p>
* hiss_form_bytearray Mapper 接口
* </p>
*
* @author lenovo
* @since 2023-06-26 14:25:21
*/
public interface HissFormBytearrayMapper extends BaseMapper<HissFormBytearray> {

    @Select("select * from hiss_form_bytearray where data_id=#{modelId} and name=#{name}")
    public HissFormBytearray getModelJson(@Param("modelId") String modelId, @Param("name") String name);

    @Delete("delete from hiss_form_bytearray where data_id=#{dataId}")
    void deleteByDataId(@Param("dataId") String dataId);
}
