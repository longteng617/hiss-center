package cn.itcast.hiss.process.activiti.service;

import cn.itcast.hiss.api.client.task.NotificationTask;
import cn.itcast.hiss.common.enums.HissTaskTypeEnum;
import cn.itcast.hiss.message.CurrentUser;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.form.FormSubmitDataMessage;
import cn.itcast.hiss.message.sender.task.*;
import cn.itcast.hiss.process.activiti.pojo.HissAutoApprovalConfig;
import cn.itcast.hiss.process.activiti.vo.MultiInstanceVo;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * activiti 的业务实现
 *
 * @author: miukoo
 * @describe: activiti
 * @date: 2022/12/28 10:10
 */
public interface ActivitiService {

    /**
     * 创建知会任务
     * @param params
     * @return
     */
    public void createNotificationTask(NotificationTaskMessage params, MessageContext messageContext);

    void createTask(NotificationTask notificationTask, CurrentUser currentUser, HissTaskTypeEnum type, MessageContext messageContext);

    /**
     * 创建抄送任务
     * @param params
     * @return
     */
    public void createCcTask(NotificationTaskMessage params, MessageContext messageContext);

    /**
     * 认领任务
     * @param params
     * @param messageContext
     */
    void claimTask(TaskMessage params, MessageContext messageContext);

    /**
     * 归还
     * @param params
     * @param messageContext
     */
    void unclaimTask(TaskMessage params, MessageContext messageContext);

    /**
     * 取消任务
     * @param params
     * @param messageContext
     */


    void cancelTask(CancelTaskMessage params, MessageContext messageContext);

    /**
     * 驳回任务
     * @param params
     * @param messageContext
     * @param first 是否默认退回流程第一个节点，true 是,false默认是上一个节点，
     */
    void rollBackTask(AnyJumpTaskMessage params, MessageContext messageContext, boolean first);

    /**
     * 跳转任务
     * @param params
     * @param messageContext
     * @param first 是否默认跳转流程第一个节点，true 是,false默认是上一个节点，
     */
    void jumpTask(AnyJumpTaskMessage params, MessageContext messageContext,boolean first);

    /**
     * 撤回任务
     * @param params
     * @param messageContext
     * @param first 是否默认退回流程第一个节点，true 是,false默认是上一个节点，
     */
    void withdrawTask(AnyJumpTaskMessage params, MessageContext messageContext,boolean first);

    void readTask(NotificationTaskMessage params, MessageContext messageContext);

    void approveTask(ApproveTaskMessage params, MessageContext messageContext);

    void delegateTask(DelegateTaskMessage params, MessageContext messageContext);

    void rejectTask(RejectTaskMessage params, MessageContext messageContext);

    MultiInstanceVo isMultiInstance(String processDefinitionId, String taskDefinitionKey);

    /**
     * 创建流程任务
     * @param currentTask
     * @param createTime
     * @return
     */
    TaskEntity createNewTask(Task currentTask, Date createTime, String assignee);



    /**
     * 并行加签任务
     * @param params
     * @param messageContext
     */
    void doParallelSignTask(AddExecutionTaskMessage params, MessageContext messageContext);

    /**
     * 信化触发接口
     * @param params
     * @param messageContext
     */
    void trigger(TiggerTaskMessage params, MessageContext messageContext);


    /**
     * 获取历史执行的任务
     *
     * @param taskId
     * @param jumpTaskOrActivityId
     * @param tenantId
     * @return
     */
    List<HissAutoApprovalConfig> saveHistoryTaskByTaskId(String taskId, String jumpTaskOrActivityId, String tenantId);

    /**
     * 自动完成任务
     * @param taskEntity
     */
    void autoComplateTask(TaskEntity taskEntity,boolean isComment);


    void autoApproval(DelegateExecution execution, String users, Expression expression);

    /**
     * 自动拒绝任务
     * @param execution
     * @param users
     * @param expression
     */
     void autoRejectTask(DelegateExecution execution, String users, Expression expression) ;
    void autoRejectTask(String taskId);
    /**
     * 判断是否需要做自动审批
     * @param task
     */
    HissAutoApprovalConfig checkIfTaskApproved(TaskEntity task);

    void cleanAutoApprovalConfig(String processInstanceId);

    List<HissAutoApprovalConfig> saveSkipFlowNodes(String taskId, String jumpTaskOrActivityId, String tenant);

    Set<String> doGetClientUserInfo(DelegateExecution execution, String assignee);


    void doCCorNotifierTask(DelegateExecution execution, String users, Expression expression,HissTaskTypeEnum type);
    void startByModel(Message message, MessageContext messageContext,boolean isInitForm);

    void startPreProcessByModel(Message message, MessageContext messageContext);

    void modelToDeploment(Message message, MessageContext messageContext);

    Execution getExecutionById(String executionId);

    void received(ReceivedTaskMessage params, MessageContext messageContext);

    void sendPreProcess(FormSubmitDataMessage params, MessageContext messageContext);

    void expediteTask(TaskMessage params, MessageContext messageContext);

    void restartProcess(TaskMessage params, MessageContext messageContext);
}
