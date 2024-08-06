package cn.itcast.hiss.process.activiti.mapper;

import cn.itcast.hiss.process.activiti.pojo.HissProcessUpdateJob;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* <p>
* hiss_process_update_job Mapper 接口
* </p>
*
* @author lenovo
* @since 2023-07-12 14:01:50
*/
public interface HissProcessUpdateJobMapper extends BaseMapper<HissProcessUpdateJob> {


    @Select("select * from hiss_process_update_job where executed_time <= now() limit 10")
    List<HissProcessUpdateJob> listJob();
}
