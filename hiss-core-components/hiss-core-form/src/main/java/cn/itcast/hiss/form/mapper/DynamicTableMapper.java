package cn.itcast.hiss.form.mapper;

import cn.itcast.hiss.api.client.form.FormDataList;
import cn.itcast.hiss.form.pojo.HissFormTableFields;
import cn.itcast.hiss.form.pojo.HissFormTables;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/*
 * @author miukoo
 * @description 动态表数据查询，用于动态表单的数据查询
 * @date 2023/7/3 11:58
 * @version 1.0
 **/
public interface DynamicTableMapper {

    @Select("select * from `${table.tablePhysicalName}` where id=#{dataId}")
    public Map listTableDataById(@Param("table") HissFormTables table,@Param("dataId")String dataId);

    @Select("select * from `${table.tablePhysicalName}` where parent_id=#{parentId}")
    public List<Map> listTableDataByParentId(@Param("table") HissFormTables table,@Param("parentId")String parentId);

    public List<Map> listTableDataForPage(@Param("table") HissFormTables table,@Param("formData") FormDataList formData);

    public long countTableDataForPage(@Param("table") HissFormTables table,@Param("formData") FormDataList formData);

    @Delete("delete from `${table.tablePhysicalName}` where id=#{dataId}")
    public int deleteById(@Param("table") HissFormTables table,@Param("dataId")String dataId);

    int copyRowData(@Param("table") HissFormTables mainTable,@Param("fields")  List<HissFormTableFields> fields,
                    @Param("oldDataId") String oldDataId, @Param("newDataId") String newDataId,@Param("parentId")  String parentId);
}
