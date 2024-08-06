package cn.itcast.hiss.process.activiti.mapper;

import cn.itcast.hiss.api.client.processdefinition.viewer.ActHiComment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/*
 * @author miukoo
 * @description 评论信息表
 * @date 2023/6/5 11:04
 * @version 1.0
 **/
public interface HiCommentMapper {


    @Insert("insert into act_hi_comment(`id_`,`action_`,`full_msg_`,`message_`,`proc_inst_id_`,`task_id_`,`time_`,`type_`,`user_id_`) values " +
            "(#{comment.id},#{comment.action},#{comment.fullMsg},#{comment.message},#{comment.procInstId},#{comment.taskId},#{comment.time},#{comment.type},#{comment.userId})")
    void insertActHiComment(@Param("comment") ActHiComment comment);

    @Select("select * from  act_hi_comment where proc_inst_id_=#{processInstanceId} order by time_ desc")
    List<ActHiComment> selectByProcessInstanceId(@Param("processInstanceId") String processInstanceId);


}
