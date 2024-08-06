package cn.itcast.hiss.process.activiti.multilnstance;



import cn.hutool.core.util.ObjectUtil;
import cn.itcast.hiss.common.SystemConstant;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * AddSequenceMultiInstanceCmd
 *
 * @author: wgl
 * @describe: 串行加签
 * @date: 2022/12/28 10:10
 */
public class AddSequenceMultiInstanceCmd implements Command {

    private String taskId;
    private Map<String, Object> variables;

    private String targetUserId;

    private String targetUserName;

    private TaskService taskService;

    private Boolean isBefore;

    public AddSequenceMultiInstanceCmd(String taskId, Map<String, Object> variables,String userId,String userName,TaskService taskService,boolean isBefore) {
        this.taskId = taskId;
        this.variables = variables;
        this.targetUserId = userId;
        this.targetUserName = userName;
        this.taskService = taskService;
        this.isBefore = isBefore;
    }

    public Object execute(CommandContext commandContext) {
        //判断是否是前加签
        TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
        TaskEntity taskEntity = taskEntityManager.findById(taskId);
        ExecutionEntity executionEntity = taskEntity.getExecution();
        List<String> assigneeList = (List) executionEntity.getVariable(SystemConstant.MULTILINSTANCE_ASSIGNEE_LIST);
        Integer nrOfInstances = (Integer) executionEntity.getVariable(SystemConstant.MULTILINSTANCE_NUMBER_OF_INSTANCES);

        if(isBefore) {
            ArrayList<String> newAssigneeList = new ArrayList<>();
            for (int i=0; i < assigneeList.size(); i++) {
                String ass = assigneeList.get(i);
                newAssigneeList.add(assigneeList.get(i));
                if(ass.equals(taskEntity.getAssignee())){
                    newAssigneeList.add(targetUserId);
                    newAssigneeList.add(taskEntity.getAssignee());
                }
            }
            executionEntity.setVariable(SystemConstant.MULTILINSTANCE_NUMBER_OF_INSTANCES, nrOfInstances + 2);
            executionEntity.setVariable(SystemConstant.MULTILINSTANCE_ASSIGNEE_LIST,newAssigneeList);
            //执行完
            return null;
        }else{
            //证明是后加签
            //需要按照后加签逻辑修改执行人列表
            List<String> newAssigneeList = new ArrayList<String>();
            for (int i=0; i < assigneeList.size(); i++) {
                String ass = assigneeList.get(i);
                newAssigneeList.add(assigneeList.get(i));
                if(ass.equals(taskEntity.getAssignee())){
                    newAssigneeList.add(targetUserId);
                }
            }
            //修改局部变量内容
            executionEntity.setVariable(SystemConstant.MULTILINSTANCE_ASSIGNEE_LIST, newAssigneeList);
            executionEntity.setVariable(SystemConstant.MULTILINSTANCE_NUMBER_OF_INSTANCES, nrOfInstances + 1);
            return null;
        }
    }
}
