package cn.itcast.hiss.process.activiti.multilnstance;

import cn.hutool.core.util.ObjectUtil;
import cn.itcast.hiss.common.SystemConstant;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.agenda.ContinueMultiInstanceOperation;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;

import java.io.Serializable;
import java.util.*;


/**
 * AddMultiInstanceExecutionCmd
 * @author: wgl
 * @describe: 并行加签处理
 * @date: 2023/6/4 10:16
 **/
public class AddMultiInstanceExecutionCmd implements Command<Execution>, Serializable {

    private String taskId;
    private Map<String, Object> variables;

    private String targetUserId;

    private String targetUserName;

    private TaskService taskService;

    public AddMultiInstanceExecutionCmd(String taskId, Map<String, Object> variables,String userId,String userName,TaskService taskService) {
        this.taskId = taskId;
        this.variables = variables;
        this.targetUserId = userId;
        this.targetUserName = userName;
        this.taskService = taskService;
    }

    @Override
    public Execution execute(CommandContext commandContext) {
        //获取当前节点的执行实例
        return insertExecution(commandContext);
    }

    private Execution insertExecution(CommandContext commandContext){
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
        TaskEntity taskEntity = taskEntityManager.findById(taskId);
        ExecutionEntity execution = taskEntity.getExecution();
        ExecutionEntity rootExecution = getMultiInstanceRootExecution(execution);
        // 创建子
        ExecutionEntity newExecution = executionEntityManager.createChildExecution(rootExecution);
        newExecution.setCurrentFlowElement(rootExecution.getCurrentFlowElement());

        Integer currentNumberOfInstances = (Integer) rootExecution.getVariable(SystemConstant.MULTILINSTANCE_NUMBER_OF_INSTANCES);
        rootExecution.setVariableLocal(SystemConstant.MULTILINSTANCE_NUMBER_OF_INSTANCES, currentNumberOfInstances+1);

        RepositoryService repositoryService = commandContext.getProcessEngineConfiguration().getRepositoryService();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(rootExecution.getProcessDefinitionId());
        Activity activity = (Activity)bpmnModel.getFlowElement(rootExecution.getActivityId());
        MultiInstanceLoopCharacteristics loopCharacteristics = activity.getLoopCharacteristics();

        // 更新激活数量
        Integer nrOfActiveInstances = (Integer) rootExecution.getVariable(SystemConstant.MULTILINSTANCE_NUMBER_OF_ACTIVE);
        rootExecution.setVariableLocal(SystemConstant.MULTILINSTANCE_NUMBER_OF_ACTIVE, nrOfActiveInstances+1);

        // 设置索引变量
        newExecution.setVariableLocal(loopCharacteristics.getElementIndexVariable(),currentNumberOfInstances);

        // 设置新任务
        String elementVariable = loopCharacteristics.getElementVariable();
        newExecution.setVariableLocal(elementVariable,this.targetUserId);

        if (!loopCharacteristics.isSequential()) {
            newExecution.setActive(true);
            newExecution.setScope(false);
            newExecution.setCurrentFlowElement(activity);
            commandContext.getAgenda().planOperation(new HissContinueMultiInstanceOperation(commandContext,newExecution,rootExecution,currentNumberOfInstances));
        }
        return newExecution;
    }

    protected ExecutionEntity getMultiInstanceRootExecution(ExecutionEntity executionEntity) {
        ExecutionEntity multiInstanceRootExecution = null;
        ExecutionEntity currentExecution = executionEntity;
        while (currentExecution != null  && multiInstanceRootExecution == null && currentExecution.getParent() != null) {
            if (currentExecution.isMultiInstanceRoot()) {
                multiInstanceRootExecution = currentExecution;
            } else {
                currentExecution = currentExecution.getParent();
            }
        }
        return multiInstanceRootExecution;
    }

}
