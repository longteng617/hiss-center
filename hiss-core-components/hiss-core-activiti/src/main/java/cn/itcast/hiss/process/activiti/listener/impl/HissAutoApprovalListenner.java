package cn.itcast.hiss.process.activiti.listener.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.itcast.hiss.api.client.HissProcessConstants;
import cn.itcast.hiss.api.client.task.RejectType;
import cn.itcast.hiss.process.activiti.pojo.HissAutoApprovalConfig;
import cn.itcast.hiss.process.activiti.service.ActivitiService;
import cn.itcast.hiss.process.activiti.service.impl.ActivitiServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HissAutoApprovalListenner
 *
 * @author: wgl
 * @describe: 自动审批监听器
 * @date: 2022/12/28 10:10
 */
@Component
@Scope("prototype")
@Slf4j
@Data
public class HissAutoApprovalListenner implements ActivitiEventListener {

    protected Expression expression;

    private ActivitiService activitiService;
    private TaskService taskService = null;

    @Override
    public void onEvent(ActivitiEvent event) {
        if (event.getType().equals(ActivitiEventType.TASK_CREATED)) {
            init();
            //判断是否是空执行节点
            boolean isAuto = checkAutoComplateUserTask(event);
            boolean isNull = checkNullUserTask(event);
            if ( isAuto || isNull) {
                TaskEntity task = (TaskEntity) ((ActivitiEntityEventImpl) event).getEntity();
                // 自动通过任务
                activitiService.autoComplateTask(task,!isAuto);
                return;
            }
            TaskEntity task = (TaskEntity) ((ActivitiEntityEventImpl) event).getEntity();
            String taskId = task.getId();
            // 查询数据库，判断任务是否已经审批过
            HissAutoApprovalConfig hissAutoApprovalConfig = activitiService.checkIfTaskApproved(task);
            if (ObjectUtil.isNotNull(hissAutoApprovalConfig)) {
                //如果是忽略所有前置任务--执行自动审批任务
                RejectType currentType = RejectType.valueOf(hissAutoApprovalConfig.getOperate());
                if (currentType == RejectType.IGNORE_ALL || currentType == RejectType.IGNORE_REVIEW) {
                    activitiService.autoComplateTask(task,true);
                }
                //如果是自动拒绝任务--执行自动拒绝任务
                if (currentType == RejectType.AUTO_REJECT) {
                    activitiService.autoRejectTask(taskId);
                }
            }
        }
        // 流程结束清理掉所有的自动配置
        if (event.getType().equals(ActivitiEventType.PROCESS_COMPLETED)) {
            init();
            String processInstanceId = event.getProcessInstanceId();
            activitiService.cleanAutoApprovalConfig(processInstanceId);
        }
    }

    private void init() {
        if (ObjectUtil.isNull(activitiService)) {
            synchronized (this) {
                if (ObjectUtil.isNull(activitiService)) {
                    Map<String, ActivitiServiceImpl> beansOfType = SpringUtil.getBeansOfType(ActivitiServiceImpl.class);
                    beansOfType.values().forEach(item -> {
                        activitiService = item;
                    });
                    taskService = SpringUtil.getBean(TaskService.class);
                }
            }
        }
    }

    /**
     * 判断是否是自动完成的stater节点
     *
     * @param event
     */
    private boolean checkAutoComplateUserTask(ActivitiEvent event) {
        ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
        Object entity = entityEvent.getEntity();
        TaskEntity taskEntity = (TaskEntity) entity;
        Object variable = taskEntity.getVariable(HissProcessConstants.getAutoComplateFlag(taskEntity.getTaskDefinitionKey()));
        if (ObjectUtil.isNotNull(variable)) {
            String autoFlag = (String) variable;//获取自动处理标志位
            boolean flag = HissProcessConstants.TASK_VAR_AUTO_COMPLATE.equals(autoFlag);
            // 自动通过一次后，删除表示，避免与跳转节点配置冲突
            if(flag){
                taskEntity.removeVariable(HissProcessConstants.getAutoComplateFlag(taskEntity.getTaskDefinitionKey()));
            }
            return flag;
        }
        return false;
    }

    /**
     * 判断是否存在空节点标识，并需要自动跳过
     *
     * @param event
     */
    private boolean checkNullUserTask(ActivitiEvent event) {
        ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
        Object entity = entityEvent.getEntity();
        TaskEntity taskEntity = (TaskEntity) entity;
        Object variable = taskEntity.getVariable(HissProcessConstants.getNullNodeAutoFlag(taskEntity.getTaskDefinitionKey()));
        if (ObjectUtil.isNotNull(variable)) {
            String autoFlag = (String) variable;//获取自动处理标志位
            if (HissProcessConstants.TASK_VAR_NULL_NODE_AUTO_COMPLATE.equals(autoFlag)) {
                ExecutionEntity execution = taskEntity.getExecution();
                FlowElement currentFlowElement = execution.getCurrentFlowElement();
                if (currentFlowElement instanceof UserTask) {
                    UserTask userTask = (UserTask) currentFlowElement;
                    //判断是否是多实例节点
                    if (userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) {
                        //获取对应
                        ParallelMultiInstanceBehavior behavior = (ParallelMultiInstanceBehavior) userTask.getBehavior();
                        if (behavior != null) {
                            Expression collectionExpression = behavior.getCollectionExpression();
                            // 获取变量解析器
                            Object value = collectionExpression.getValue(execution);
                            return ObjectUtil.isNull(value);
                        }
                    } else if (userTask.getBehavior() instanceof SequentialMultiInstanceBehavior) {
                        SequentialMultiInstanceBehavior behavior = (SequentialMultiInstanceBehavior) userTask.getBehavior();
                        if (behavior != null) {
                            Expression collectionExpression = behavior.getCollectionExpression();
                            // 获取变量解析器
                            Object value = collectionExpression.getValue(execution);
                            return ObjectUtil.isNull(value);
                        }
                    } else {
                        // 获取执行人
                        String assignee = userTask.getAssignee();
                        // 获取候选人
                        List<String> candidateUsers = userTask.getCandidateUsers();
                        // 获取候选组
                        List<String> candidateGroups = userTask.getCandidateGroups();
                        return StrUtil.isEmpty(assignee) && CollectionUtil.isEmpty(candidateUsers) && CollectionUtil.isEmpty(candidateGroups);
                    }
                }
            }
        }
        return false;
    }

    private List<String> extractCandidateUsers(List<IdentityLink> identityLinks) {
        // 从 IdentityLink 列表中提取候选用户
        // 根据业务需求进行适当的处理和转换
        // 这里仅示例直接返回候选用户的用户ID
        return identityLinks.stream()
                .filter(link -> link.getUserId() != null)
                .map(IdentityLink::getUserId)
                .collect(Collectors.toList());
    }

    private List<String> extractCandidateGroups(List<IdentityLink> identityLinks) {
        // 从 IdentityLink 列表中提取候选组
        // 根据业务需求进行适当的处理和转换
        // 这里仅示例直接返回候选组的组ID
        return identityLinks.stream()
                .filter(link -> link.getGroupId() != null)
                .map(IdentityLink::getGroupId)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

}
