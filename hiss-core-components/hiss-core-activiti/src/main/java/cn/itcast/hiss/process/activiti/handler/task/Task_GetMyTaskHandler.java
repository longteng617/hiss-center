package cn.itcast.hiss.process.activiti.handler.task;

import cn.itcast.hiss.api.client.common.PageInfo;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.CurrentUser;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.common.PageInfoMessage;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * GetTask
 *
 * @author: wgl
 * @describe: 获取我的代办任务
 * @date: 2022/12/28 10:10
 */
@Component
public class Task_GetMyTaskHandler implements CmdHandler<PageInfoMessage> {

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        List<Map> taskList = new ArrayList<>();
        PageInfo payload = (PageInfo) params.getPalyload();
        // 获取当前操作用户信息
        CurrentUser currentUser = params.getMessageAuth().getCurrentUser();
        // 分页查询待捞取任务
        long count = taskService.createTaskQuery()
                .taskCandidateOrAssigned(currentUser.getUserId())
                .taskTenantId(params.getMessageAuth().getTenant())
                .count();

        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateOrAssigned(currentUser.getUserId())
                .taskTenantId(params.getMessageAuth().getTenant())
                .listPage(payload.getPageNum(), payload.getPageSize());

        // 处理查询结果
        for (Task task : tasks) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            HashMap<String, Object> taskMap = new HashMap<>();
            taskMap.put("id", task.getId());
            taskMap.put("name", task.getName());
            taskMap.put("status", task.getDelegationState());
            taskMap.put("createdDate", task.getCreateTime());
            taskMap.put("assignee", task.getAssignee());
            taskMap.put("instanceName", processInstance.getName());
            taskList.add(taskMap);
        }
        messageContext.setResult(new ConcurrentHashMap<>() {{
            put("list", taskList);
            put("total", count);
        }});
    }

    /**
     * 处理登录人为任务处理人的情况
     * @param pageInfo
     * @param map
     */
    private void dealAssignee(CurrentUser currentUser,PageInfo pageInfo,Map map){
        List<Map> taskList = new ArrayList<>();
        // 分页查询待捞取任务
        long count = taskService.createTaskQuery()
                .taskAssignee(currentUser.getUserName()).count();
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(currentUser.getUserName())
                .listPage(pageInfo.getPageNum(), pageInfo.getPageSize());
        // 处理查询结果
        for (Task task : tasks) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            HashMap<String, Object> taskMap = new HashMap<>();
            taskMap.put("id", task.getId());
            taskMap.put("name", task.getName());
            taskMap.put("status", task.getDelegationState());
            taskMap.put("createdDate", task.getCreateTime());
            taskMap.put("assignee", task.getAssignee());
            taskMap.put("instanceName", processInstance.getName());
            taskList.add(taskMap);
        }
        map.put("assign", new HashMap<>(){
            {
                put("list", taskList);
                put("total", count);
            }
        });
    }

    /**
     * 处理登录人为任务候选人的情况
     * @param currentUser
     * @param pageInfo
     * @param map
     */
    private void dealCandidateUser(CurrentUser currentUser,PageInfo pageInfo,Map map){
        List<Map> taskList = new ArrayList<>();
        // 分页查询待捞取任务
        long count = taskService.createTaskQuery()
                .taskCandidateUser(currentUser.getUserName()).count();
        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateUser(currentUser.getUserName())
                .listPage(pageInfo.getPageNum(), pageInfo.getPageSize());
        // 处理查询结果
        for (Task task : tasks) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            HashMap<String, Object> taskMap = new HashMap<>();
            taskMap.put("id", task.getId());
            taskMap.put("name", task.getName());
            taskMap.put("status", task.getDelegationState());
            taskMap.put("createdDate", task.getCreateTime());
            taskMap.put("assignee", task.getAssignee());
            taskMap.put("instanceName", processInstance.getName());
            taskList.add(taskMap);
        }
        map.put("candidateUser", new HashMap<>(){
            {
                put("list", taskList);
                put("total", count);
            }
        });
    }


    /**
     * 处理登录人为任务候选人的情况
     * @param currentUser
     * @param pageInfo
     * @param map
     */
    private void dealCandidateUserGroup(CurrentUser currentUser,PageInfo pageInfo,Map map){
        List<Map> taskList = new ArrayList<>();
        // 分页查询待捞取任务
        long count = taskService.createTaskQuery()
                .taskCandidateGroup(currentUser.getUserId()).count();
        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateGroup(currentUser.getUserId())
                .listPage(pageInfo.getPageNum(), pageInfo.getPageSize());
        // 处理查询结果
        for (Task task : tasks) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            HashMap<String, Object> taskMap = new HashMap<>();
            taskMap.put("id", task.getId());
            taskMap.put("name", task.getName());
            taskMap.put("status", task.getDelegationState());
            taskMap.put("createdDate", task.getCreateTime());
            taskMap.put("assignee", task.getAssignee());
            taskMap.put("instanceName", processInstance.getName());
            taskList.add(taskMap);
        }
        map.put("candidateUserGroup", new HashMap<>(){
            {
                put("list", taskList);
                put("total", count);
            }
        });
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.TASK_GET_MY_TASK.getId();
    }
}
