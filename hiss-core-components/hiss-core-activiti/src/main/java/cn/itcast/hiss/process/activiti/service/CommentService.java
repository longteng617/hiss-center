package cn.itcast.hiss.process.activiti.service;

import cn.itcast.hiss.api.client.processdefinition.viewer.ActHiComment;
import org.activiti.engine.task.Task;

import java.util.List;

/*
 * @author miukoo
 * @description 流程评论操作接口
 * @date 2023/6/5 11:57
 * @version 1.0
 **/
public interface CommentService {

    public void addComment(Task task,String userId,String title,String content);

    public void addComment(String taskId, String processInstanceId, String userId, String title, String content);

    public void addComment(String processInstanceId, String userId, String title, String content);
    List<ActHiComment> selectByProcessInstanceId(String processInstanceId);

}
