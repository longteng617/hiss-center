package cn.itcast.hiss.process.activiti.multilnstance;

import org.activiti.bpmn.model.FlowNode;
import org.activiti.engine.impl.agenda.ContinueMultiInstanceOperation;
import org.activiti.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/*
 * @author miukoo
 * @description 把任务发起执行
 * @date 2023/6/23 21:21
 * @version 1.0
 **/
public class HissContinueMultiInstanceOperation extends ContinueMultiInstanceOperation {
    ExecutionEntity rootExecution;
    int loopCounter;
    public HissContinueMultiInstanceOperation(CommandContext commandContext, ExecutionEntity newExecution,ExecutionEntity rootExecution,int loopCounter) {
        super(commandContext, newExecution);
        this.loopCounter = loopCounter;
        this.rootExecution = rootExecution;
    }

    protected void continueThroughMultiInstanceFlowNode(FlowNode flowNode) {
        updateLoopCounter(flowNode);
        super.continueThroughMultiInstanceFlowNode(flowNode);
    }


    /**
     * 更新节点的循环下标
     * @param flowNode
     */
    protected void updateLoopCounter(FlowNode flowNode) {
        ActivityBehavior activityBehavior = (ActivityBehavior) flowNode.getBehavior();
        MultiInstanceActivityBehavior multiInstanceActivityBehavior = (MultiInstanceActivityBehavior) activityBehavior;
        String elementIndexVariable = multiInstanceActivityBehavior.getCollectionElementIndexVariable();
        if (!flowNode.isAsynchronous()) {
            execution.setVariable(elementIndexVariable, loopCounter);
        } else {
            rootExecution.setVariableLocal(elementIndexVariable, loopCounter);
        }
    }
}
