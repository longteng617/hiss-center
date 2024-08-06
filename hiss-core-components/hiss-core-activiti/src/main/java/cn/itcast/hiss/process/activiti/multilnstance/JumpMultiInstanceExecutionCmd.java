package cn.itcast.hiss.process.activiti.multilnstance;

import org.activiti.bpmn.model.*;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntityManager;
import org.activiti.engine.task.Task;

import java.util.List;

/*
 * @author miukoo
 * @description 跳出多实例节点，到指定节点
 * @date 2023/7/5 14:14
 * @version 1.0
 **/
public class JumpMultiInstanceExecutionCmd implements Command {
    BpmnModel bpmnModel;
    List<SequenceFlow> targetSequenceFlow;
    Task task;
    public JumpMultiInstanceExecutionCmd(BpmnModel bpmnModel, List<SequenceFlow> targetSequenceFlow, Task task ){
        this.bpmnModel = bpmnModel;
        this.task = task;
        this.targetSequenceFlow = targetSequenceFlow;
    }


    @Override
    public Void execute(CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        ExecutionEntity execution = executionEntityManager.findById(task.getExecutionId());
        Activity activity = (Activity) bpmnModel.getFlowElement(execution.getActivityId());
        MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = activity.getLoopCharacteristics();
        if(multiInstanceLoopCharacteristics!=null){
            // 获取所有的子任务
            ExecutionEntity rootExecution = getMultiInstanceRootExecution(execution);
            List<ExecutionEntity> childExecutions = executionEntityManager.findChildExecutionsByParentExecutionId(rootExecution.getId());
            // 删除所有的任务
            if(childExecutions!=null){
                for (ExecutionEntity childExecution : childExecutions) {
                    executionEntityManager.deleteChildExecutions(childExecution, "Delete for jump");
                    executionEntityManager.deleteExecutionAndRelatedData(childExecution, "Delete for jump");
                }
            }
            executionEntityManager.deleteChildExecutions(rootExecution, "Delete for jump");
            executionEntityManager.deleteExecutionAndRelatedData(rootExecution, "Delete for jump");
            // 如果是串行
            if(multiInstanceLoopCharacteristics.isSequential()){

            }
            ExecutionEntity parent = rootExecution.getParent();
            if(parent!=null){
                SequenceFlow sequenceFlow = targetSequenceFlow.get(0);
                ExecutionEntity executionEntity = executionEntityManager.createChildExecution(parent);
                executionEntity.setActive(true);
                executionEntity.setScope(false);
                executionEntity.setCurrentFlowElement(sequenceFlow.getTargetFlowElement());
                commandContext.getAgenda().planContinueProcessOperation(executionEntity);
            }
        }
        return null;
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
