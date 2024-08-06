package cn.itcast.hiss.process.activiti.service.impl;

import cn.itcast.hiss.api.client.processdefinition.viewer.ActHiComment;
import cn.itcast.hiss.process.activiti.mapper.HiCommentMapper;
import cn.itcast.hiss.process.activiti.service.CommentService;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * @author miukoo
 * @description 添加评论信息
 * @date 2023/6/5 11:58
 * @version 1.0
 **/
@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    HiCommentMapper hiCommentMapper;
    @Override
    public void addComment(Task task,String userId,String title,String content) {
        ActHiComment comment = new ActHiComment();
        comment.setMessage(title);
        comment.setFullMsg(content);
        comment.setUserId(userId);
        comment.setTaskId(task.getId());
        comment.setProcInstId(task.getProcessInstanceId());
        hiCommentMapper.insertActHiComment(comment);
    }

    @Override
    public void addComment(String taskId,String processInstanceId,String userId,String title,String content) {
        ActHiComment comment = new ActHiComment();
        comment.setMessage(title);
        comment.setFullMsg(content);
        comment.setUserId(userId);
        comment.setTaskId(taskId);
        comment.setProcInstId(processInstanceId);
        hiCommentMapper.insertActHiComment(comment);
    }

    @Override
    public void addComment(String processInstanceId,String userId, String title, String content) {
        ActHiComment comment = new ActHiComment();
        comment.setMessage(title);
        comment.setFullMsg(content);
        comment.setUserId(userId);
        comment.setProcInstId(processInstanceId);
        hiCommentMapper.insertActHiComment(comment);
    }

    @Override
    public List<ActHiComment> selectByProcessInstanceId(String processInstanceId) {
        return hiCommentMapper.selectByProcessInstanceId(processInstanceId);
    }
}
