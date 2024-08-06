package cn.itcast.hiss.process.activiti.mapper;

import cn.itcast.hiss.api.client.processdefinition.viewer.ActHiComment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/*
 * @author miukoo
 * @description 操作配置表中的数据
 * @date 2023/6/5 11:04
 * @version 1.0
 **/
public interface GeBytearrayMapper {


    @Insert("insert into act_ge_bytearray(`id_`,`bytes_`,`model_id`,`name_`,`rev_`) values " +
            "(#{id},#{configJson},#{modelId},'configJson',1)")
    void insertModelConfigJson(@Param("id") String id,@Param("configJson") String configJson,@Param("modelId") String modelId);

    @Select("select bytes_ from  act_ge_bytearray where `model_id`=#{modelId} and name_='configJson' limit 1")
    String getModelConfigJson(@Param("modelId") String modelId);

    @Delete("delete from act_ge_bytearray where `model_id`=#{modelId} and name_='configJson'")
    void deleteModelConfigJson(@Param("modelId") String modelId);

}
