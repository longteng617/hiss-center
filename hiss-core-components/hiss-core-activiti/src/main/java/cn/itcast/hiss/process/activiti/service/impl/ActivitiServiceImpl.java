package cn.itcast.hiss.process.activiti.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.HissProcessConstants;
import cn.itcast.hiss.api.client.common.HissVariableServer;
import cn.itcast.hiss.api.client.form.FormSubmitData;
import cn.itcast.hiss.api.client.processdefinition.ProcessDesignModel;
import cn.itcast.hiss.api.client.task.*;
import cn.itcast.hiss.api.server.common.UserInfo;
import cn.itcast.hiss.common.SystemConstant;
import cn.itcast.hiss.common.enums.*;
import cn.itcast.hiss.event.pojo.HissActivitiEvent;
import cn.itcast.hiss.form.service.HissFormModelService;
import cn.itcast.hiss.message.CurrentUser;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.form.FormSubmitDataMessage;
import cn.itcast.hiss.message.sender.processdefinition.ProcessDesignModelMessage;
import cn.itcast.hiss.message.sender.task.*;
import cn.itcast.hiss.process.activiti.configurator.function.HissUtil;
import cn.itcast.hiss.process.activiti.listener.HissActivitiEventBuilder;
import cn.itcast.hiss.process.activiti.mapper.*;
import cn.itcast.hiss.process.activiti.multilnstance.AddMultiInstanceExecutionCmd;
import cn.itcast.hiss.process.activiti.multilnstance.JumpMultiInstanceExecutionCmd;
import cn.itcast.hiss.process.activiti.pojo.*;
import cn.itcast.hiss.process.activiti.service.ActivitiService;
import cn.itcast.hiss.process.activiti.service.CommentService;
import cn.itcast.hiss.process.activiti.service.UpdateProcessTenantService;
import cn.itcast.hiss.process.activiti.service.UserTaskService;
import cn.itcast.hiss.process.activiti.util.AdminUtil;
import cn.itcast.hiss.process.activiti.util.VariableUtil;
import cn.itcast.hiss.process.activiti.variables.SysVariableManager;
import cn.itcast.hiss.process.activiti.vo.MultiInstanceVo;
import cn.itcast.hiss.server.template.HissServerApperanceTemplate;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.engine.*;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.history.*;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/*
 * @author miukoo
 * @description 流程办理各功能实现
 * @date 2023/6/3 11:19
 * @version 1.0
 **/
@Slf4j
@Service
@Transactional
public class ActivitiServiceImpl implements ActivitiService {

    @Autowired
    HissProcessUpdateJobMapper hissProcessUpdateJobMapper;

    @Autowired
    HissProcessPreLaunchMapper hissProcessPreLaunchMapper;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private HistoryService historyService;
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HiActinstMapper hiActinstMapper;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private HissAutoApprovalConfigMapper hissAutoApprovalConfigMapper;

    @Autowired
    private UserTaskService userTaskService;

    @Autowired
    private ActRuEventSubscrMapper actRuEventSubscrMapper;

    @Autowired
    private HissServerApperanceTemplate hissServerApperanceTemplate;
    @Autowired
    private GeBytearrayMapper geBytearrayMapper;

    @Autowired
    private HissProcessFormMapper hissProcessFormMapper;

    @Autowired
    private HissFormModelService hissFormModelService;

    @Autowired
    private ActReModelMapper actReModelMapper;


    /**
     * 创建知会任务
     *
     * @param params
     * @return
     */
    @Override
    public void createNotificationTask(NotificationTaskMessage params, MessageContext messageContext) {
        createTask(params, HissTaskTypeEnum.NOTIFICATION, messageContext);
    }

    private void createTask(NotificationTaskMessage params, HissTaskTypeEnum type, MessageContext messageContext) {
        NotificationTask notificationTask = params.getPalyload();
        createTask(notificationTask, params.getMessageAuth().getCurrentUser(), type, messageContext);
    }

    @Override
    public void createTask(NotificationTask notificationTask, CurrentUser currentUser, HissTaskTypeEnum type, MessageContext messageContext) {
        String actionName = notificationTask.getOperatorName();
        Task task = taskService.createTaskQuery().taskId(notificationTask.getTaskId()).singleResult();
        if (task != null) {
            if (StrUtil.isNotEmpty(task.getAssignee()) && task.getAssignee().equalsIgnoreCase(currentUser.getUserId())) {
                ActReModel actReModel = actReModelMapper.getModelByBusId(type.name().toLowerCase());
                ProcessDefinition hissNotification = repositoryService.createProcessDefinitionQuery()
                        .processDefinitionName("hiss_"+type.name().toLowerCase())
                        .deploymentId(actReModel.getDeploymentId())
                        .processDefinitionTenantId("tenant_hiss")
                        .latestVersion().singleResult();
                // 增加流程标识
                Map<String,Object> map = new HashMap<>();
                map.put(type.name().toLowerCase(),notificationTask.getUserId());
                map.put(HissProcessConstants.PARENT_PROCESS_INSTANCE,task.getProcessInstanceId());
                map.put(HissProcessConstants.NODE_TYPE, type.name());// 这里type实际是ActiveNodeTypeEnum的值，但是可以直接使用type，因为抄送和知会
                ProcessInstance oldProcessInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
                Authentication.setAuthenticatedUserId(task.getAssignee());
                ProcessInstance processInstance = runtimeService.startProcessInstanceById(hissNotification.getId(), task.getBusinessKey(), map);
                addHissProcessUpdateJob("【"+actionName+"】"+oldProcessInstance.getName(),task.getTenantId(),processInstance.getId(),10000);

                // 添加评论
                String comment = String.format("【%s】%s了%s", currentUser.getUserName(), actionName, notificationTask.getUserName());
                commentService.addComment(task, currentUser.getUserId(), comment, "");
                messageContext.addResult("msg", actionName + "成功");
            } else {
                messageContext.addError("msg", "你无权限" + actionName);
            }
        } else {
            messageContext.addError("msg", "任务未找到");
        }
    }

    /**
     * 创建抄送任务
     *
     * @param params
     * @return
     */
    @Override
    public void createCcTask(NotificationTaskMessage params, MessageContext messageContext) {
        createTask(params, HissTaskTypeEnum.CC, messageContext);
    }

    @Override
    public void claimTask(TaskMessage params, MessageContext messageContext) {
        HissTask hissTask = params.getPalyload();
        Task task = taskService.createTaskQuery().taskId(hissTask.getTaskId()).singleResult();
        if (task != null) {
            if (StrUtil.isEmpty(task.getAssignee())) {
                List<IdentityLink> identityLinks = runtimeService.getIdentityLinksForProcessInstance(task.getProcessInstanceId());
                boolean isOk = false;
                if (identityLinks != null) {
                    CurrentUser currentUser = params.getMessageAuth().getCurrentUser();
                    Set<String> userGroup = new HashSet<>();
                    if (StrUtil.isNotEmpty(currentUser.getUserGroups())) {
                        userGroup.addAll(Arrays.asList(currentUser.getUserGroups().split(",")));
                    }
                    for (IdentityLink identityLink : identityLinks) {
                        if (StrUtil.isNotEmpty(identityLink.getUserId())) {// 是否是候选者
                            if (identityLink.getUserId().equals(currentUser.getUserId())) {
                                isOk = true;
                                break;
                            }
                        }
                        if (StrUtil.isNotEmpty(identityLink.getGroupId()) && userGroup.size() > 0) {
                            if (userGroup.contains(identityLink.getGroupId())) {// 是否是候选组
                                isOk = true;
                                break;
                            }
                        }
                    }
                }
                if (!isOk) {
                    messageContext.addError("msg", "无权限" + hissTask.getOperatorName() + "任务");
                    return;
                }
                taskService.claim(hissTask.getTaskId(), params.getMessageAuth().getCurrentUser().getUserId());
                // 添加认领评论
                String comment = String.format("【%s】%s了任务", params.getMessageAuth().getCurrentUser().getUserName(), hissTask.getOperatorName());
                commentService.addComment(task, params.getMessageAuth().getCurrentUser().getUserId(), comment, "");
                messageContext.addResultAndCount("msg", "任务" + hissTask.getOperatorName() + "成功");
            } else {
                messageContext.addError("msg", "任务已经被" + hissTask.getOperatorName());
            }
        } else {
            messageContext.addError("msg", "任务未找到");
        }
    }

    /**
     * 归还任务
     *
     * @param params
     * @param messageContext
     */
    @Override
    public void unclaimTask(TaskMessage params, MessageContext messageContext) {
        HissTask hissTask = params.getPalyload();
        Task task = taskService.createTaskQuery().taskId(hissTask.getTaskId()).singleResult();
        if (task != null) {
            if (StrUtil.isNotEmpty(task.getAssignee()) && task.getAssignee().equalsIgnoreCase(params.getMessageAuth().getCurrentUser().getUserId())) {
                taskService.unclaim(hissTask.getTaskId());
                // 添加认领评论
                String comment = String.format("【%s】%s了任务", params.getMessageAuth().getCurrentUser().getUserName(), hissTask.getOperatorName());
                commentService.addComment(task, params.getMessageAuth().getCurrentUser().getUserId(), comment, "");
                messageContext.addResultAndCount("msg", "任务" + hissTask.getOperatorName() + "成功");
            } else {
                messageContext.addError("msg", "你无权限" + hissTask.getOperatorName());
            }
        } else {
            messageContext.addError("msg", "任务未找到");
        }
    }

    /**
     * @Description: 判断当前节点是否为会签节点
     * @param: processDefinitionId 流程定义id
     * @param: taskDefinitionKey 当前节点id
     * @return: com.ruoyi.workflow.domain.vo.MultiInstanceVo
     * @author: gssong
     * @Date: 2022/4/16 13:31
     */
    public MultiInstanceVo isMultiInstance(String processDefinitionId, String taskDefinitionKey) {
        BpmnModel bpmnModel = processEngine.getRepositoryService().getBpmnModel(processDefinitionId);
        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(taskDefinitionKey);
        MultiInstanceVo multiInstanceVo = new MultiInstanceVo();
        //判断是否为并行会签节点
        if (flowNode.getBehavior() instanceof ParallelMultiInstanceBehavior) {
            ParallelMultiInstanceBehavior behavior = (ParallelMultiInstanceBehavior) flowNode.getBehavior();
            if (behavior != null && behavior.getCollectionVariable() != null) {
                String assigneeList = behavior.getCollectionVariable();
                String assignee = behavior.getCollectionElementVariable();
                multiInstanceVo.setType(behavior);
                multiInstanceVo.setAssignee(assignee);
                multiInstanceVo.setAssigneeList(assigneeList);
                return multiInstanceVo;
            }
            //判断是否为串行会签节点
        } else if (flowNode.getBehavior() instanceof SequentialMultiInstanceBehavior) {
            SequentialMultiInstanceBehavior behavior = (SequentialMultiInstanceBehavior) flowNode.getBehavior();
            if (behavior != null && behavior.getCollectionVariable() != null) {
                String assigneeList = behavior.getCollectionVariable();
                String assignee = behavior.getCollectionElementVariable();
                multiInstanceVo.setType(behavior);
                multiInstanceVo.setAssignee(assignee);
                multiInstanceVo.setAssigneeList(assigneeList);
                return multiInstanceVo;
            }
        }
        return null;
    }

    /**
     * @Description: 根据Task任务创建流程任务
     * @param: parentTask
     * @param: createTime
     * @Date: 2022/3/13
     */
    @Override
    public TaskEntity createNewTask(Task currentTask, Date createTime, String assignee) {
        TaskEntity task = null;
        if (ObjectUtil.isNotEmpty(currentTask)) {
            task = (TaskEntity) processEngine.getTaskService().newTask();
            task.setCategory(currentTask.getCategory());
//            task.setExecutionId(currentTask.getExecutionId());// 不能使用
            task.setDescription(currentTask.getDescription());
            task.setTenantId(currentTask.getTenantId());
            task.setAssignee(assignee);
            task.setName(currentTask.getName());
            task.setProcessDefinitionId(currentTask.getProcessDefinitionId());
            task.setProcessInstanceId(currentTask.getProcessInstanceId());
            task.setTaskDefinitionKey(currentTask.getTaskDefinitionKey());
            task.setPriority(currentTask.getPriority());
            task.setCreateTime(createTime);
            processEngine.getTaskService().saveTask(task);
            return task;
        }
        return null;
    }


    /*** 取消任务
     * @param params
     * @param messageContext
     */
    @Override
    public void cancelTask(CancelTaskMessage params, MessageContext messageContext) {
        CancelTask cancelTask = params.getPalyload();
        Task task = taskService.createTaskQuery().taskId(cancelTask.getTaskId()).singleResult();
        if (task != null) {
            Map<String, Object> variables = taskService.getVariables(task.getId());
            String userId = (String) variables.get(SystemConstant.TASK_VARIABLES_CREATE_USERID);
            String currentUserId = params.getMessageAuth().getCurrentUser().getUserId();
            // 只有发起人能取消
            if (currentUserId.equals(userId)) {
                // 标记流程是否已经取消
                runtimeService.setVariable(task.getExecutionId(), HissProcessConstants.PROCESS_STATUS, ProcessStatusEnum.CANCEL.name());
                // 添加评论
                String comment = String.format("【%s】%s了流程", params.getMessageAuth().getCurrentUser().getUserName(), cancelTask.getOperatorName());
                commentService.addComment(task, params.getMessageAuth().getCurrentUser().getUserId(), comment, "");
                // 添加流程变量，表示任务被拒绝
                runtimeService.deleteProcessInstance(task.getProcessInstanceId(), cancelTask.getReason());
                messageContext.addResultAndCount("msg", "任务" + cancelTask.getOperatorName() + "成功");
            } else {
                messageContext.addError("msg", "你无权限" + cancelTask.getOperatorName() + "任务");
            }
        } else {
            messageContext.addError("msg", "任务未找到");
        }
    }

    @Override
    public void rollBackTask(AnyJumpTaskMessage params, MessageContext messageContext, boolean first) {
        anyJump(params, messageContext, first, params.getPalyload().getOperatorName());
    }

    @Override
    public void withdrawTask(AnyJumpTaskMessage params, MessageContext messageContext, boolean first) {
        anyJump(params, messageContext, first, params.getPalyload().getOperatorName());
    }

    @Override
    public void jumpTask(AnyJumpTaskMessage params, MessageContext messageContext, boolean first) {
        anyJump(params, messageContext, first, params.getPalyload().getOperatorName());
    }

    /**
     * 进行知晓操作
     *
     * @param params
     * @param messageContext
     */
    @Override
    public void readTask(NotificationTaskMessage params, MessageContext messageContext) {
        NotificationTask palyload = params.getPalyload();
        Task task = taskService.createTaskQuery().taskId(palyload.getTaskId())
                .taskAssignee(params.getMessageAuth().getCurrentUser().getUserId()).singleResult();
        if (task != null) {
            String currentUserId = params.getMessageAuth().getCurrentUser().getUserId();
            // 只有发起人能取消
            if (currentUserId.equals(task.getAssignee())) {
                // 完成
                taskService.complete(task.getId());
                // 添加评论
                String comment = String.format("%s【%s】", params.getMessageAuth().getCurrentUser().getUserName(), palyload.getOperatorName());
                commentService.addComment(task, params.getMessageAuth().getCurrentUser().getUserId(), comment, "");
                messageContext.addResultAndCount("msg", palyload.getOperatorName() + "操作成功");
            } else {
                messageContext.addError("msg", "你无权限操作任务");
            }
        } else {
            messageContext.addError("msg", "任务未找到");
        }
    }


    /**
     * 批准数据，批准的同时也有可能需要重表单中获取变量数据
     * @param params
     * @param messageContext
     */
    @Override
    public void approveTask(ApproveTaskMessage params, MessageContext messageContext) {
        ApproveTask payload = params.getPalyload();
        String taskId = payload.getTaskId();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null) {
            if (StrUtil.isNotEmpty(task.getAssignee()) && task.getAssignee().equalsIgnoreCase(params.getMessageAuth().getCurrentUser().getUserId())) {
                if (StrUtil.isNotEmpty(payload.getOperatorName())) {
                    String comment = String.format("%s【%s】", params.getMessageAuth().getCurrentUser().getUserName(), payload.getOperatorName());
                    commentService.addComment(task, params.getMessageAuth().getCurrentUser().getUserId(), comment, StringUtils.isNotBlank(payload.getReason()) ? payload.getReason() : "");
                }
                // 如果前端有变更的数据，需要重置到流程变量中
                Map formData = new HashMap();
                if(StrUtil.isNotEmpty(payload.getFormId())){
                    String[] forms = payload.getFormId().split(",");
                    List<HissProcessForm> hissProcessForms = hissProcessFormMapper.listFormByProcessInstanceId(task.getProcessInstanceId());
                    if(hissProcessForms!=null){
                        FormSubmitDataMessage message = new FormSubmitDataMessage();
                        message.setMessageAuth(params.getMessageAuth());
                        message.setPalyload(new FormSubmitData());
                        for (HissProcessForm hissProcessForm : hissProcessForms) {
                            for (String formId : forms) {
                                if(formId.equals(hissProcessForm.getFormId())){
                                    if(StrUtil.isNotEmpty(hissProcessForm.getDataId())){
                                        Map temp = getFormData(message, hissProcessForm.getFormId(),hissProcessForm.getDataId());
                                        if(temp!=null){
                                            formData.putAll(temp);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Map<String, Object> variables = payload.getVariables();
                if(variables!=null){
                    variables.putAll(formData);
                }else{
                    payload.setVariables(formData);
                }
                // 检查任务是否是委派任务
                if (task.getDelegationState() == DelegationState.PENDING) {
                    // 获取委派用户信息
                    String delegateUser = task.getOwner();
                    // 完成委派任务
                    taskService.resolveTask(taskId, payload.getVariables());
                    taskService.complete(taskId, payload.getVariables());
                    log.info("完成委派任务，委派用户：{}", delegateUser);
                } else {
                    // 完成普通任务
                    taskService.complete(taskId, payload.getVariables());
                    log.info("完成任务：{}, 完成人{}", task.getName(), task.getAssignee());
                }
                messageContext.addResultAndCount("msg", "任务" + payload.getOperatorName() + "成功");
            } else {
                messageContext.addError("msg", "你无权限" + payload.getOperatorName() + "任务");
            }
        } else {
            messageContext.addError("msg", "任务未找到");
        }
    }

    @Override
    public void delegateTask(DelegateTaskMessage params, MessageContext messageContext) {
        DelegateTask delegateTask = params.getPalyload();
        Task task = taskService.createTaskQuery().taskId(delegateTask.getTaskId()).singleResult();

        if (task != null) {
            if (StrUtil.isNotEmpty(task.getAssignee()) && task.getAssignee().equalsIgnoreCase(params.getMessageAuth().getCurrentUser().getUserId())) {
                try {
                    String taskId = delegateTask.getTaskId();
                    String assignee = delegateTask.getUserId();
                    // 指派任务给指定用户
                    taskService.delegateTask(taskId, assignee);
                    messageContext.addResultAndCount("msg", "任务" + delegateTask.getOperatorName() + "成功");
                } catch (Exception e) {
                    messageContext.addError("msg", "任务" + delegateTask.getOperatorName() + "失败");
                }
            } else {
                messageContext.addError("msg", "你无权限" + delegateTask.getOperatorName() + "任务");
            }
        } else {
            messageContext.addError("msg", "任务未找到");
        }
    }

    @Override
    public void rejectTask(RejectTaskMessage params, MessageContext messageContext) {
        RejectTask payload = params.getPalyload();
        Task task = taskService.createTaskQuery().taskId(payload.getTaskId()).singleResult();
        if (task != null) {
            //如果是并行多实例节点，则直不允许加签
            MultiInstanceVo multiInstance = isMultiInstance(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
            if (multiInstance!=null && multiInstance.getType() instanceof ParallelMultiInstanceBehavior) {
                //如果是并行多实例节点，则不删除流程
                complateTaskButNotDeleteProcess(task, payload);
                return;
            } else if (StrUtil.isNotEmpty(task.getAssignee()) && task.getAssignee().equalsIgnoreCase(params.getMessageAuth().getCurrentUser().getUserId())) {
                //根据任务id获取流程实例id
                String processDefinitionId = task.getProcessDefinitionId();
                String processInstanceId = task.getProcessInstanceId();
                // 审批意见
                if (StrUtil.isNotEmpty(payload.getReason())) {
                    String operatorName = payload.getOperatorName();
                    if (StrUtil.isNotEmpty(operatorName)) {
                        String comment = String.format("%s【%s】", params.getMessageAuth().getCurrentUser().getUserName(), operatorName);
                        commentService.addComment(task, params.getMessageAuth().getCurrentUser().getUserId(), comment, StringUtils.isNotBlank(payload.getReason()) ? payload.getReason() : "");
                    }
                }
                //当拒绝的时候设置拒绝参数
                Map<String, Object> variable = new HashMap<>();
                variable.put(SystemConstant.REJECT_TASK_ID, payload.getTaskId());
                variable.put(SystemConstant.REJECT_TASK_REASON, payload.getReason());
                variable.put(SystemConstant.REJECT_TASK_REJECT_TIME, LocalDateTime.now());
                variable.put(SystemConstant.REJECT_TASK_REJECT_PROCESSINSTANCE_ID, processInstanceId);
                variable.put(SystemConstant.REJECT_TASK_REJECT_PROCESSDEFINITION_ID, processDefinitionId);
                variable.put(SystemConstant.REJECT_TASK_REJECT_PROCESSDEFINITION_ID, processDefinitionId);
                Map<String, Object> rejectParams = payload.getVariables();
                variable.putAll(rejectParams);
                //记录流程变量
                runtimeService.setVariables(processInstanceId, variable);
                // 添加流程变量，表示任务被拒绝
                runtimeService.deleteProcessInstance(processInstanceId, "任务被" + payload.getOperatorName() + ":" + payload.getReason());
                messageContext.addResultAndCount("msg", "任务" + payload.getOperatorName() + "成功");
            } else {
                messageContext.addError("msg", "你无权限" + payload.getOperatorName() + "任务");
            }
        } else {
            messageContext.addError("msg", "任务未找到");
        }
    }

    /**
     * 完成任务但是不删除流程
     *
     * @param task
     * @param payload
     */
    private void complateTaskButNotDeleteProcess(Task task, RejectTask payload) {
        Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
        //在流程变量中 获取自动完成数量 并 -1 再完成任务
        Integer complateNumber = (Integer) variables.get(SystemConstant.COMPLATE_INSTANCES);
        //如果不为空的话
        if (ObjectUtil.isNotNull(complateNumber)) {
            complateNumber--;
        }
        runtimeService.setVariable(task.getExecutionId(), SystemConstant.COMPLATE_INSTANCES, complateNumber);
        // 审批意见
        taskService.complete(task.getId(), payload.getVariables());
    }

    /**
     * 跳转任意节点
     *
     * @param params
     * @param messageContext
     * @param first
     */
    public void anyJump(AnyJumpTaskMessage params, MessageContext messageContext, boolean first, String operatorName) {
        AnyJumpTask palyload = params.getPalyload();
        Task task = taskService.createTaskQuery().taskId(palyload.getTaskId()).singleResult();
        String processDefinitionId = task.getProcessDefinitionId();
        String processInstanceId = task.getProcessInstanceId();
        String activityId = palyload.getJumpTaskOrActivityId();
        if (StrUtil.isEmpty(activityId)) {
            // 对上一个节点和发起节点的支持
            HistoricActivityInstance targetActivity = getRejectTargetActivity(palyload.getJumpTaskOrActivityId(), processInstanceId, first);
            if (targetActivity != null) {
                activityId = targetActivity.getActivityId();
            }
        }
        if (StrUtil.isEmpty(activityId)) {
            messageContext.addError("msg", operatorName + "节点信息未找到");
            return;
        }
        try {
            if (activityId.equals(task.getTaskDefinitionKey())) {
                messageContext.addError("msg", "不能" + operatorName + "到自己节点");
                return;
            }
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
            Process process = bpmnModel.getMainProcess();
            // 解析调整的目标节点
            FlowNode targetFlowNode = (FlowNode) process.getFlowElement(activityId);
            List<SequenceFlow> incomingFlows = targetFlowNode.getIncomingFlows();
            List<SequenceFlow> targetSequenceFlow = new ArrayList<>();
            for (SequenceFlow incomingFlow : incomingFlows) {
                FlowNode source = (FlowNode) incomingFlow.getSourceFlowElement();
                List<SequenceFlow> sequenceFlows;
                if (source instanceof ParallelGateway) {// 如果是并行网关同级节点，则跳转到所有节点
                    sequenceFlows = source.getOutgoingFlows();
                } else {
                    sequenceFlows = targetFlowNode.getIncomingFlows();// 否则直接跳转到对应节点，包括为执行过的节点
                }
                targetSequenceFlow.addAll(sequenceFlows);
            }
            FlowNode curFlowNode = (FlowNode) process.getFlowElement(task.getTaskDefinitionKey());
            // 判读是否为多实例节点的跳转
            boolean isMulti = false;
            if(curFlowNode!=null){
                if(curFlowNode instanceof UserTask){
                    UserTask userTask = (UserTask)curFlowNode;
                    MultiInstanceLoopCharacteristics loopCharacteristics = userTask.getLoopCharacteristics();
                    if(loopCharacteristics!=null){
                        isMulti = true;
                        managementService.executeCommand(new JumpMultiInstanceExecutionCmd(bpmnModel,targetSequenceFlow,task));
                        String comment = String.format("%s%s流程", params.getMessageAuth().getCurrentUser().getUserName(), operatorName);
                        commentService.addComment(task, params.getMessageAuth().getCurrentUser().getUserId(), comment, StringUtils.isNotBlank(palyload.getReason()) ? palyload.getReason() : "");
                    }
                }
            }
            if(!isMulti) {
                List<Task> list = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
                for (Task t : list) {
                    trunToTarget(process, t, params, targetSequenceFlow, operatorName);
                }
            }
            messageContext.addResultAndCount("msg", operatorName + "成功");
        } catch (Exception e) {
            e.printStackTrace();
            messageContext.addError("msg", operatorName + "失败");
        }
    }

    /**
     * 把一个任务动态转向目标节点
     *
     * @param process
     * @param task
     * @param targetSequenceFlow
     * @param operatorName
     */
    private void trunToTarget(Process process, Task task, AnyJumpTaskMessage params, List<SequenceFlow> targetSequenceFlow, String operatorName) {
        AnyJumpTask palyload = params.getPalyload();
        FlowNode curFlowNode = (FlowNode) process.getFlowElement(task.getTaskDefinitionKey());
        if(curFlowNode==null){
            for (FlowElement flowElement : process.getFlowElements()) {
                if(flowElement instanceof SubProcess){
                    SubProcess subProcess = (SubProcess) flowElement;
                    FlowElement fe = subProcess.getFlowElement(task.getTaskDefinitionKey());
                    if(fe!=null){
                        curFlowNode= (FlowNode) fe;
                        break;
                    }
                }
            }
        }
        List<SequenceFlow> tempOutgoingSequenceFlows = new ArrayList<>(curFlowNode.getOutgoingFlows());
        if (palyload.getTaskId().equals(task.getId())) {
            curFlowNode.setOutgoingFlows(targetSequenceFlow);
            String comment = String.format("%s%s流程", params.getMessageAuth().getCurrentUser().getUserName(), operatorName);
            commentService.addComment(task, params.getMessageAuth().getCurrentUser().getUserId(), comment, StringUtils.isNotBlank(palyload.getReason()) ? palyload.getReason() : "");
            taskService.complete(palyload.getTaskId());
        } else {
            if (StringUtils.isBlank(task.getParentTaskId())) {// 子任务不跳转
                curFlowNode.setOutgoingFlows(new ArrayList<>());
                taskService.complete(task.getId());
                historyService.deleteHistoricTaskInstance(task.getId());
                hiActinstMapper.deleteHiActivityInstByTaskId(task.getId());
            }
        }
        curFlowNode.setOutgoingFlows(tempOutgoingSequenceFlows);
    }

    /**
     * 获取历史驳回或回退目标节点,支持上一节点，第一个节点
     *
     * @param taskId            要回退的taskId
     * @param processInstanceId
     * @return
     */
    private HistoricActivityInstance getRejectTargetActivity(String taskId, String processInstanceId, boolean first) {
        HistoricActivityInstance targetActivity = null;
        HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId);

        // 取得所有历史任务按时间降序排序
        List<HistoricActivityInstance> historicActivityInstances = null;
        if (first) {// 退到第一个节点
            historicActivityInstances = query.orderByHistoricActivityInstanceStartTime().asc().list();
        } else { // 推到上一个节点
            historicActivityInstances = query.orderByHistoricActivityInstanceStartTime().desc().list();
        }

        if (CollectionUtils.isEmpty(historicActivityInstances) || historicActivityInstances.size() < 2) {
            return null;
        }
        if (StringUtils.isBlank(taskId)) {
            // 不传活动id的情况直接找第一个任务
            // 最后一条是当前正在进行的任务 需要找到最近的但是名称和当前任务不一样的任务去驳回
            HistoricActivityInstance current = historicActivityInstances.get(0);
            for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
                if (!current.getActivityId().equals(historicActivityInstance.getActivityId())) {
                    if (historicActivityInstance.getActivityType().equals("userTask")) {
                        targetActivity = historicActivityInstance;
                        break;
                    }
                }
            }
            log.info("查到符合要求的节点 type:{}   id:{}", targetActivity.getActivityType(), targetActivity.getActivityId());
        }
        return targetActivity;
    }

    public void actionName(Task task) {
        task.getProcessDefinitionId();

    }

    /**
     * 并行加签任务
     *
     * @param params
     * @param messageContext
     */
    @Override
    public void doParallelSignTask(AddExecutionTaskMessage params, MessageContext messageContext) {
        AddExecutionTask addExecutionTask = (AddExecutionTask) params.getPalyload();
        Task task = taskService.createTaskQuery().taskId(addExecutionTask.getTaskId())
                .taskCandidateOrAssigned(params.getMessageAuth().getCurrentUser().getUserId()).singleResult();
        if (task.isSuspended()) {
            messageContext.setResult(new ConcurrentHashMap<>() {
                {
                    put("msg", "任务已挂起");
                }
            });
            return;
        }
        String taskDefinitionKey = task.getTaskDefinitionKey();
        String processInstanceId = task.getProcessInstanceId();
        String processDefinitionId = task.getProcessDefinitionId();
        //判断是否是多实例节点，如果不是多实例节点，则不允许加签
        MultiInstanceVo multiInstanceVo = isMultiInstance(processDefinitionId, taskDefinitionKey);
        if (ObjectUtil.isEmpty(multiInstanceVo)) {
            messageContext.setResult(new ConcurrentHashMap<>() {
                {
                    put("msg", "当前环节不是会签节点");
                }
            });
        }
        try {
            //如果是并行多实例节点，则直不允许加签
            if (multiInstanceVo.getType() instanceof SequentialMultiInstanceBehavior) {
                //如果是并行多实例节点，则直接抛出异常
                messageContext.setResult(new ConcurrentHashMap<>() {
                    {
                        put("msg", "串行行多实例节点不允许加签");
                    }
                });
                return;
            } else if (multiInstanceVo.getType() instanceof ParallelMultiInstanceBehavior) {
                //如果是并行多实例节点，则直接进行加签操作
                AddMultiInstanceExecutionCmd addMultiInstanceExecutionCmd = new AddMultiInstanceExecutionCmd(task.getId(), runtimeService.getVariables(task.getExecutionId()), addExecutionTask.getUserId(), addExecutionTask.getUserName(), taskService);
                managementService.executeCommand(addMultiInstanceExecutionCmd);
            }
            String userName = params.getMessageAuth().getCurrentUser().getUserName();
            commentService.addComment(task,params.getMessageAuth().getCurrentUser().getUserId(),userName + addExecutionTask.getOperatorName()+"【" + String.join(",", addExecutionTask.getUserName()) + "】","");
            messageContext.setResult(new ConcurrentHashMap<>() {
                {
                    put("msg", "加签成功");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            messageContext.setResult(new ConcurrentHashMap<>() {
                {
                    put("msg", "加签失败");
                }
            });
        }
    }

    /**
     * 信化触发接口
     *
     * @param params
     * @param messageContext
     */
    @Override
    public void trigger(TiggerTaskMessage params, MessageContext messageContext) {
        try {
            runtimeService.trigger(params.getPalyload().getExecutionId());
            messageContext.addResultAndCount("msg", "已触发");
        } catch (Exception e) {
            e.printStackTrace();
            messageContext.addError("msg", "信化触发失败");
        }
    }

    /**
     * 获取历史任务
     *
     * @param taskId
     * @param jumpTaskOrActivityId
     */
    @Override
    public List<HissAutoApprovalConfig> saveHistoryTaskByTaskId(String taskId, String jumpTaskOrActivityId, String tenantId) {
        Task nowTask = taskService.createTaskQuery().taskId(taskId).taskTenantId(tenantId).singleResult();
        // 获取两个任务之间的所有历史任务
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(nowTask.getProcessInstanceId())
                .finished()
                .orderByHistoricTaskInstanceEndTime()
                .asc();
        HistoricTaskInstance targetHistoric = null;
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().processInstanceId(nowTask.getProcessInstanceId()).taskDefinitionKey(jumpTaskOrActivityId).singleResult();
        if (ObjectUtil.isEmpty(historicTaskInstance)) {
            throw new RuntimeException("未找到历史任务");
        } else {
            targetHistoric = historicTaskInstance;
        }

        List<HistoricTaskInstance> historicTasks = query.list();
        List<HissAutoApprovalConfig> resultList = new ArrayList<>();
        boolean startRecording = false;
        // 遍历历史任务，记录任务ID从开始到结束
        for (HistoricTaskInstance task : historicTasks) {
            if (task.getId().equals(targetHistoric.getId())) {
                startRecording = true;
                //跳过当前节点
                continue;
            }
            if (startRecording) {
                HissAutoApprovalConfig hissAutoApprovalConfig = new HissAutoApprovalConfig();
                hissAutoApprovalConfig.setActivityId(task.getTaskDefinitionKey());
                hissAutoApprovalConfig.setTaskId(task.getId());
                hissAutoApprovalConfig.setCreatedTime(new Date());
                hissAutoApprovalConfig.setOperate(RejectType.IGNORE_REVIEW.getType());
                hissAutoApprovalConfig.setProcessInstanceId(nowTask.getProcessInstanceId());
                hissAutoApprovalConfig.setTenantId(tenantId);
                resultList.add(hissAutoApprovalConfig);
            }
            if (task.getId().equals(taskId)) {
                break;
            }
        }
        //插入到数据库中
        for (HissAutoApprovalConfig index : resultList) {
            hissAutoApprovalConfigMapper.insertHissAutoApprovalConfig(index);
        }
        return resultList;
    }

    /**
     * 自动完成任务
     */
    @Override
    public void autoComplateTask(TaskEntity task,boolean isComment) {
        if (task.isSuspended()) {
            throw new RuntimeException("任务已挂起");
        }
        if(isComment) {
            commentService.addComment(task, "hiss", "自动完成", "系统自动完成任务");
        }
        taskService.complete(task.getId());
    }

    /**
     * 自动完成任务
     */
    @Override
    public void autoApproval(DelegateExecution execution, String users, Expression expression) {
        // 当前激活的任务
        Set<String> nameSet = new HashSet<String>();
        if (StrUtil.isNotEmpty(users)) {
            Collections.addAll(nameSet, users.split(","));
        }
        if (ObjectUtil.isNotEmpty(expression)) {
            //请求客户端获取对应的用户信息
            nameSet = doGetClientUserInfo(execution, expression.getExpressionText());
        }
        String name = "系统";
        if (nameSet.size() > 0) {
            name = nameSet.iterator().next();
        }
        commentService.addComment("", execution.getProcessInstanceId(), "hiss", "自动完成", name + "自动完成任务");
    }

    /**
     * 单人办理自动拒绝
     *
     * @param execution
     * @param users
     * @param expression
     */
    @Override
    public void autoRejectTask(DelegateExecution execution, String users, Expression expression) {
        // 当前激活的任务
        Set<String> nameSet = new HashSet<String>();
        if (StrUtil.isNotEmpty(users)) {
            Collections.addAll(nameSet, users.split(","));
        }
        if (ObjectUtil.isNotEmpty(expression)) {
            //请求客户端获取对应的用户信息
            nameSet = doGetClientUserInfo(execution, expression.getExpressionText());
        }
        String name = "系统";
        if (nameSet.size() > 0) {
            name = nameSet.iterator().next();
        }
        commentService.addComment("", execution.getProcessInstanceId(), "hiss", "自动拒绝", name + "自动拒绝任务");
        endProcess(execution.getProcessDefinitionId(), execution.getProcessInstanceId(), null);
    }

    @Override
    public void autoRejectTask(String taskId) {
        autoRejectTask(taskId, "系统");
    }

    /**
     * 自动拒绝实现
     *
     * @param taskId
     * @param user
     */
    private void autoRejectTask(String taskId, String user) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (ObjectUtil.isEmpty(task)) {
            throw new RuntimeException("任务不存在");
        }
        if (task.isSuspended()) {
            throw new RuntimeException("任务已挂起");
        }
        commentService.addComment(task.getId(), "hiss", "自动拒绝", user + "自动拒绝任务");
        endProcess(task.getProcessDefinitionId(), task.getProcessInstanceId(), taskId);
    }

    private void endProcess(String processDefinitionId, String processInstanceId, String taskId) {
        //当拒绝的时候设置拒绝参数
        Map<String, Object> variable = new HashMap<>();
        if (StrUtil.isNotEmpty(taskId)) {
            variable.put(SystemConstant.REJECT_TASK_ID, taskId);
        }
        variable.put(SystemConstant.REJECT_TASK_REASON, "自动拒绝");
        variable.put(SystemConstant.REJECT_TASK_REJECT_TIME, LocalDateTime.now());
        variable.put(SystemConstant.REJECT_TASK_REJECT_PROCESSINSTANCE_ID, processInstanceId);
        variable.put(SystemConstant.REJECT_TASK_REJECT_PROCESSDEFINITION_ID, processDefinitionId);
        variable.put(SystemConstant.REJECT_TASK_REJECT_PROCESSDEFINITION_ID, processDefinitionId);
        //记录流程变量
        runtimeService.setVariables(processInstanceId, variable);
        // 添加流程变量，表示任务被拒绝
        runtimeService.deleteProcessInstance(processInstanceId, "任务被hiss系统自动拒绝");
    }

    /**
     * 判断是否需要自动审批
     *
     * @param task
     */
    @Override
    public HissAutoApprovalConfig checkIfTaskApproved(TaskEntity task) {
        LambdaQueryWrapper<HissAutoApprovalConfig> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(HissAutoApprovalConfig::getProcessInstanceId, task.getProcessInstance());
        lambdaQueryWrapper.eq(HissAutoApprovalConfig::getTenantId, task.getTenantId());
        lambdaQueryWrapper.eq(HissAutoApprovalConfig::getActivityId, task.getTaskDefinitionKey());
        return hissAutoApprovalConfigMapper.selectByInstanceIdAndKey(task.getProcessInstance().getProcessInstanceId(), task.getTenantId(), task.getTaskDefinitionKey());
    }

    /**
     * 清除自动审批记录
     *
     * @param processInstanceId
     */
    @Override
    public void cleanAutoApprovalConfig(String processInstanceId) {
        LambdaQueryWrapper<HissAutoApprovalConfig> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(HissAutoApprovalConfig::getProcessInstanceId, processInstanceId);
        hissAutoApprovalConfigMapper.delete(lambdaQueryWrapper);
    }


    /**
     * 记录跳过中间审核环节节点
     *
     * @param taskId
     * @param jumpTaskOrActivityId
     * @param tenantId
     * @return
     */
    @Override
    public List<HissAutoApprovalConfig> saveSkipFlowNodes(String taskId, String jumpTaskOrActivityId, String tenantId) {
        Task nowTask = taskService.createTaskQuery().taskId(taskId).taskTenantId(tenantId).singleResult();
        // 获取两个任务之间的所有历史任务
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(nowTask.getProcessInstanceId())
                .finished()
                .orderByHistoricTaskInstanceEndTime()
                .asc();
        HistoricTaskInstance targetHistoric = null;
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(nowTask.getProcessInstanceId())
                .taskDefinitionKey(jumpTaskOrActivityId)
                .singleResult();
        if (ObjectUtil.isEmpty(historicTaskInstance)) {
            throw new RuntimeException("未找到跳转节点的历史任务");
        } else {
            targetHistoric = historicTaskInstance;
        }
        List<HistoricTaskInstance> historicTasks = query.list();
        if (CollectionUtils.isEmpty(historicTasks)) {
            throw new RuntimeException("未找到历史任务");
        }
        for (HistoricTaskInstance index : historicTasks) {

        }
        return null;
    }

    @Override
    public Set<String> doGetClientUserInfo(DelegateExecution execution, String assignee) {
        Set<String> resClientNames = new HashSet<>();
        Map<String, Object> variables = execution.getVariables();
        //判断是否是客户端变量--如果是客户端变量，那么就提取客户端请求信息
        if (VariableUtil.checkIsClientVariable(assignee)) {
            String[] varList = VariableUtil.getVariables(assignee);
            Object variableValue = variables.get(varList[0]);
            if (ObjectUtil.isNotNull(variableValue)) {
                //说明是流程变量
                if (variableValue.toString().startsWith(SystemConstant.CLIENT_FLAG)) {
                    //TODO 向客户端请求数据--需要获取流程发起人的信息
                    UserInfo userInfo = userTaskService.getBeforeNodeUserInfo(execution);
                    String[] split = variableValue.toString().split("/");
                    userInfo.setCurrentNodeVariable(split[1]);
                    userInfo.setCurrentNodeValue(varList[1]);
                    log.info("当前节点需要的用户信息:{},需要往客户端请求数据", assignee);
                    List<String> name = hissServerApperanceTemplate.getUserInfo(execution.getTenantId(), userInfo);
                    resClientNames.addAll(name);
                } else {
                    //说明是固定的值--不需要请求客户端
                    resClientNames.add(variableValue.toString());
                }
            } else {
                //虽然是变量 但客户端并未上报
                resClientNames.add(assignee);
            }
        } else {
            //不需要请求客户端直接添加
            resClientNames.add(assignee);
        }
        return resClientNames;
    }

    @Override
    public void doCCorNotifierTask(DelegateExecution execution, String users, Expression expression, HissTaskTypeEnum type) {
        // 当前激活的任务
        Set<String> nameSet = new HashSet<String>();
        if(StrUtil.isNotEmpty(users)){
            Collections.addAll(nameSet, users.split(","));
        }
        if(ObjectUtil.isNotEmpty(expression)){
            //请求客户端获取对应的用户信息
            nameSet = doGetClientUserInfo(execution, expression.getExpressionText());
        }
        //创建知会任务
        for (String userId : nameSet) {
            ActReModel actReModel = actReModelMapper.getModelByBusId(type.name().toLowerCase());
            ProcessDefinition hissNotification = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionName("hiss_"+type.name().toLowerCase())
                    .deploymentId(actReModel.getDeploymentId())
                    .processDefinitionTenantId("tenant_hiss")
                    .latestVersion().singleResult();
            Map<String,Object> map = new HashMap<>();
            map.put(type.name().toLowerCase(),userId);
            map.put(HissProcessConstants.PARENT_PROCESS_INSTANCE,execution.getProcessInstanceId());
            map.put(HissProcessConstants.NODE_TYPE,type.name());
            ExecutionEntityImpl impl = (ExecutionEntityImpl)execution;
            ExecutionEntityImpl oldProcess = impl.getProcessInstance();
            ProcessInstance processInstance = runtimeService.startProcessInstanceById(hissNotification.getId(), "", map);
            String title = processInstance.getName();
            if(StrUtil.isEmpty(title)){
                Map<String, Object> variables = runtimeService.getVariables(oldProcess.getProcessInstanceId());
                if(variables!=null){
                    title = (String) variables.get(SystemConstant.PROCESS_VARIABLES_CREATE_TITLE);
                }
            }
            if(StrUtil.isEmpty(title)){
                title=processInstance.getProcessInstanceId();
            }
            title = "【"+type.getName()+"】"+title;
            addHissProcessUpdateJob(title,oldProcess.getTenantId(),processInstance.getId(),20000);
        }
        // 添加评论
        String comment = String.format("系统自动完成%s任务", type.getName());
        commentService.addComment(execution.getProcessInstanceId(), "hiss", "自动完成", comment);
    }

    /**
     * 插入到表中
     * @param title
     * @param tenantId
     * @param processInstanceId
     */
    private void addHissProcessUpdateJob(String title,String tenantId,String processInstanceId,int delay){
        HissProcessUpdateJob job = new HissProcessUpdateJob();
        job.setName(title);
        job.setDelay(delay);
        job.setTenantId(tenantId);
        job.setProcessInstanceId(processInstanceId);
        job.setCreatedTime(new Date());
        job.setExecutedTime(new Date(System.currentTimeMillis()+delay));
        hissProcessUpdateJobMapper.insert(job);
    }

    /**
     * 设计文件部署
     *
     * @param message
     * @param messageContext
     */
    @Override
    public void modelToDeploment(Message message, MessageContext messageContext) {
        ProcessDesignModel processDesignModel = (ProcessDesignModel) message.getPalyload();
        Model model = repositoryService.getModel(processDesignModel.getModelId());
        if (model != null) {
            Deployment deploy = modelToDeployment(model, message, messageContext, false);
            if (deploy == null) {// 发布有错误
                return;
            } else {
                messageContext.addResult("deployId", deploy.getId());
            }
        } else {
            messageContext.addError("msg", "未找到对应的model");
        }
    }

    /**
     * 模型发布
     *
     * @param model
     * @param message
     * @param messageContext
     */
    private Deployment modelToDeployment(Model model, Message message, MessageContext messageContext, boolean isCheckAuth) {
        ModelTypeEnum modelTypeEnum = ModelTypeEnum.stringToEnum(model.getKey());
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
                .key(model.getKey())
                .name(model.getName())
                .category(model.getCategory())
                .enableDuplicateFiltering()
                .tenantId(model.getTenantId());
        if (ModelTypeEnum.DEV == modelTypeEnum) {
            deploymentBuilder.addBytes(HissProcessConstants.PROCESS_RUN_XML, repositoryService.getModelEditorSource(model.getId()));
        }
        if (ModelTypeEnum.BIS == modelTypeEnum) {
            byte[] runXml = repositoryService.getModelEditorSourceExtra(model.getId());
            if (isCheckAuth) {
                boolean canStart = checkStartAuth(runXml, message);
                if (!canStart) {
                    messageContext.addError("msg", "你无权限发起预备流程");
                    return null;
                }
            }
            deploymentBuilder.addBytes(HissProcessConstants.PROCESS_SHOW_XML, repositoryService.getModelEditorSource(model.getId()));
            deploymentBuilder.addBytes(HissProcessConstants.PROCESS_RUN_XML, runXml);
        }
        String modelConfigJson = geBytearrayMapper.getModelConfigJson(model.getId());
        if (StrUtil.isNotEmpty(modelConfigJson)) {
            deploymentBuilder.addBytes(HissProcessConstants.PROCESS_SHOW_CONFIG_JSON, modelConfigJson.getBytes());
        }
        Deployment deploy = deploymentBuilder.deploy();
        model.setDeploymentId(deploy.getId());
        repositoryService.saveModel(model);
        return deploy;
    }

    /**
     * 通过model实现流程的启动
     *
     * @param message
     * @param messageContext
     * @param isInitForm 是否初始化表单数据
     */
    @Override
    public void startByModel(Message message, MessageContext messageContext,boolean isInitForm) {
        ProcessDesignModel processDesignModel = (ProcessDesignModel) message.getPalyload();
        Model model = repositoryService.getModel(processDesignModel.getModelId());
        if (model != null) {
            Deployment deploy = null;
            try{
                deploy = modelToDeployment(model, message, messageContext, true);
                if (deploy == null) {// 发布有错误
                    return;
                }
            }catch (ActivitiException e){
                e.printStackTrace();
                messageContext.addError("msg", "发布出错："+e.getMessage());
                return;
            }
            List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deploy.getId())
                    .active()
                    .processDefinitionTenantId(model.getTenantId())
                    .processDefinitionResourceName(HissProcessConstants.PROCESS_RUN_XML).list();
            ProcessDefinition processDefinition = null;
            if(list.size()==0){
                processDefinition = list.get(0);
            }else{
                for (ProcessDefinition definition : list) {
                    BpmnModel bpmnModel = repositoryService.getBpmnModel(definition.getId());
                    if(bpmnModel.getMainProcess().isExecutable()){
                        processDefinition = definition;
                        break;
                    }
                }
            }
            if (processDefinition != null) {
                String title = parseProcessName(model.getName(),message.getMessageAuth().getCurrentUser());
                HashMap<String, Object> variables = new HashMap<>();
                variables.put(SystemConstant.PROCESS_VARIABLES_CREATE_TITLE, title);
                variables.put(SystemConstant.TASK_VARIABLES_CREATE_USERID, message.getMessageAuth().getCurrentUser().getUserId());
                variables.put(SystemConstant.TASK_VARIABLES_CREATE_USERNAME, message.getMessageAuth().getCurrentUser().getUserName());
                // 设置流程变量
                buildVariables(variables, message);
                if (processDesignModel.getVariables() != null) {
                    variables.putAll(processDesignModel.getVariables());
                }
                // 设置用户ID
                Authentication.setAuthenticatedUserId(message.getMessageAuth().getCurrentUser().getUserId());
                ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId(), processDesignModel.getBusinessKey(), variables);
                addHissProcessUpdateJob(title,processInstance.getTenantId(),processInstance.getId(),10000);
                // 初始化表单：预备流程不需要初始化
                if(isInitForm) {
                    setProcessForm(model, processInstance.getTenantId(), processInstance.getProcessInstanceId(), null);
                }
                // 添加评论
                String content = String.format("%s发起了流程", message.getMessageAuth().getCurrentUser().getUserName());
                commentService.addComment(processInstance.getProcessInstanceId(), message.getMessageAuth().getCurrentUser().getUserId(), "流程开始", content);
                messageContext.addResultAndCount("processInstanceId", processInstance.getProcessInstanceId());
            } else {
                messageContext.addError("msg", "未找到对应的流程定义");
            }
        } else {
            messageContext.addError("msg", "未找到对应的model");
        }
    }

    /**
     * 设置流程使用到的表单信息
     * @param model
     * @param tenantId
     * @param processInstanceId
     * @param launchId
     */
    private void setProcessForm(Model model,String tenantId,String processInstanceId,String launchId){
        ModelTypeEnum modelTypeEnum = ModelTypeEnum.stringToEnum(model.getKey());
        String modelConfigJson = geBytearrayMapper.getModelConfigJson(model.getId());
        if (StrUtil.isNotEmpty(modelConfigJson)) {
            JSONObject jsonObject = JSON.parseObject(modelConfigJson);
            if(jsonObject.containsKey("forms")){
                JSONArray forms = jsonObject.getJSONArray("forms");
                if(forms!=null){
                    for (int i = 0; i < forms.size(); i++) {
                        JSONObject form = forms.getJSONObject(i);
                        HissProcessForm hissProcessForm =  new HissProcessForm();
                        hissProcessForm.setFormId(form.getString("id"));
                        hissProcessForm.setFormName(form.getString("name"));
                        hissProcessForm.setFormType(form.getString("type"));
                        hissProcessForm.setNodeId(form.getString("nodeId"));
                        hissProcessForm.setTenantId(tenantId);
                        hissProcessForm.setProcessInstanceId(processInstanceId);
                        hissProcessForm.setLaunchId(launchId);
                        hissProcessForm.setCreatedTime(new Date());
                        hissProcessFormMapper.insert(hissProcessForm);
                    }
                }
            }
        }
    }


    private void buildVariables(HashMap<String, Object> variables, Message params) {
        List<HissVariableServer> variable = SysVariableManager.getTenantVariable(params.getMessageAuth().getTenant());
        List<String> keyList = variable.stream().map(HissVariableServer::getKey).collect(Collectors.toList());
        ProcessDesignModel palyload = (ProcessDesignModel) params.getPalyload();
        //处理客户端提交的流程变量
        if (ObjectUtil.isNotNull(palyload.getVariables())) {
            for (String indexKey : palyload.getVariables().keySet()) {
                //判断是否是客户端上报的变量
                if (keyList.contains(indexKey)) {
                    //说明是客户端上报的变量--做特殊处理  系统标识/客户端上报Key/用户填写的Value
                    variables.put(indexKey, SystemConstant.CLIENT_FLAG + "/" + palyload.getVariables().get(indexKey) + (ObjectUtil.isNotNull(palyload.getVariables().get(indexKey)) ? "/" + palyload.getVariables().get(indexKey).toString() : ""));
                } else {
                    //不是客户端上报的变量--直接存储
                    variables.put(indexKey, palyload.getVariables().get(indexKey));
                }
            }
        }
        // 设置流程发起人信息
        variables.put(SystemConstant.TASK_VARIABLES_CREATE_USERID, params.getMessageAuth().getCurrentUser().getUserId());
        variables.put(SystemConstant.TASK_VARIABLES_CREATE_USERNAME, params.getMessageAuth().getCurrentUser().getUserName());
        //获取客户端上报的的所有流程变量
        for (HissVariableServer index : variable) {
            variables.put(index.getKey(), SystemConstant.CLIENT_FLAG + "/" + index.getValue());
        }
    }

    /**
     * 启动预备模式
     *
     * @param message
     * @param messageContext
     */
    @Override
    public void startPreProcessByModel(Message message, MessageContext messageContext) {
        ProcessDesignModel processDesignModel = (ProcessDesignModel) message.getPalyload();
        Model model = repositoryService.getModel(processDesignModel.getModelId());
        if (model != null) {
            if (StrUtil.isEmpty(model.getDeploymentId())) {
                messageContext.addError("msg", "只有部署后的流程才可以发起预备流程");
                return;
            }
            ModelTypeEnum modelTypeEnum = ModelTypeEnum.stringToEnum(model.getKey());
            if (ModelTypeEnum.DEV == modelTypeEnum) {
                messageContext.addError("msg", "开发模式的流程不支持预备模式");
                return;
            }
            boolean canStart = checkStartAuth(repositoryService.getModelEditorSourceExtra(model.getId()), message);
            if (!canStart) {
                messageContext.addError("msg", "你无权限发起预备流程");
                return;
            }
            HissProcessPreLaunch launch = new HissProcessPreLaunch();
            launch.setBusinessKey(processDesignModel.getBusinessKey());
            launch.setModelId(model.getId());
            launch.setTenantId(model.getTenantId());
            launch.setModelName(parseProcessName(model.getName(),message.getMessageAuth().getCurrentUser()));
            launch.setUserId(message.getMessageAuth().getCurrentUser().getUserId());
            launch.setUserName(message.getMessageAuth().getCurrentUser().getUserName());
            launch.setCreatedTime(new Date());
            hissProcessPreLaunchMapper.insert(launch);
            // 初始化表单
            setProcessForm(model,launch.getTenantId(),null,""+launch.getId());
            messageContext.addResultAndCount("preProcessInstanceId", launch.getId());
        } else {
            messageContext.addError("msg", "未找到对应的model");
        }
    }

    /**
     * 解析让流程名称支持动态参数
     * @param name
     * @return
     */
    private String parseProcessName(String name,CurrentUser currentUser){
        if(StrUtil.isNotEmpty(currentUser.getUserName())) {
            name = name.replace("[姓名]", currentUser.getUserName());
        }
        if(StrUtil.isNotEmpty(currentUser.getDeptName())) {
            name = name.replace("[部门]", currentUser.getDeptName());
        }
        if(StrUtil.isNotEmpty(currentUser.getRoleName())) {
            name = name.replace("[角色]", currentUser.getRoleName());
        }
        if(StrUtil.isNotEmpty(currentUser.getPostName())) {
            name = name.replace("[岗位]", currentUser.getPostName());
        }
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        name = name.replace("[日期]", date);
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        name = name.replace("[时间]", time);
        return name;
    }

    /**
     * 启动流程时判断是否有启动权限
     * 是否有权限是配置在xml文件中的extensionElements元素中
     *
     * @param message
     * @return
     */
    private boolean checkStartAuth(byte[] xmlData, Message message) {
        if (message.getMessageAuth().getCurrentUser() == null || StrUtil.isEmpty(message.getMessageAuth().getCurrentUser().getUserId())) {
            return false;
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlData);
        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
        XMLInputFactory xif = XMLInputFactory.newInstance();
        InputStreamReader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        XMLStreamReader xtr = null;
        try {
            xtr = xif.createXMLStreamReader(in);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        BpmnModel bpmnModel = bpmnXMLConverter.convertToBpmnModel(xtr);
        Process mainProcess = bpmnModel.getMainProcess();
        Map<String, List<ExtensionElement>> extensionElements = mainProcess.getExtensionElements();
        List<ExtensionElement> properties = extensionElements.get("property");
        if (properties != null) {
            int count = 0;// 如果设置了则count++,否则没有设置，则不校验
            for (ExtensionElement property : properties) {
                if (property.getName().equalsIgnoreCase("property")) {
                    String name = property.getAttributeValue(BpmnXMLConstants.ACTIVITI_EXTENSIONS_NAMESPACE, "name");
                    // 限定流程的发起人
                    if (StrUtil.isNotEmpty(name) && HissProcessConstants.PROCESSDEFINITION_WHITE_LIST_USERS.equalsIgnoreCase(name)) {
                        String value = property.getAttributeValue(BpmnXMLConstants.ACTIVITI_EXTENSIONS_NAMESPACE, "value");
                        if (StrUtil.isNotEmpty(value)) {
                            count++;
                            String userId = message.getMessageAuth().getCurrentUser().getUserId();
                            String[] split = value.split(",");
                            for (String uId : split) {
                                if (uId.equalsIgnoreCase(userId)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            return count == 0;
        }
        return true;
    }


    @Override
    public Execution getExecutionById(String executionId) {
        Execution execution = runtimeService.createExecutionQuery()
                .executionId(executionId)
                .singleResult();
        return execution;
    }

    @Override
    public void received(ReceivedTaskMessage params, MessageContext messageContext) {
        ReceivedTask palyload = params.getPalyload();
        Task task = taskService.createTaskQuery().taskId(palyload.getTaskId())
                .taskAssignee(params.getMessageAuth().getCurrentUser().getUserId()).singleResult();
        if (task != null) {
            if(palyload.getType()== ReceivedTypeEnum.MSG){
                List<ActRuEventSubscr> actRuEventSubscrs = actRuEventSubscrMapper.selectMessageByProcAndTenant(task.getProcessInstanceId(), params.getMessageAuth().getTenant());
                if(actRuEventSubscrs!=null){
                    for (ActRuEventSubscr actRuEventSubscr : actRuEventSubscrs) {
                        runtimeService.messageEventReceived(palyload.getName(),actRuEventSubscr.getExecutionId(),palyload.getVariables());
                    }
                }
            }
            if(palyload.getType()== ReceivedTypeEnum.SIGN){
                List<ActRuEventSubscr> actRuEventSubscrs = actRuEventSubscrMapper.selectSignalByProcAndTenant(task.getProcessInstanceId(), params.getMessageAuth().getTenant());
                if(actRuEventSubscrs!=null){
                    for (ActRuEventSubscr actRuEventSubscr : actRuEventSubscrs) {
                        runtimeService.signalEventReceived(palyload.getName(),actRuEventSubscr.getExecutionId(),palyload.getVariables());
                    }
                }
            }
            if(palyload.getType()== ReceivedTypeEnum.COP){
                runtimeService.trigger(task.getExecutionId());
            }
            messageContext.addResultAndCount("msg", "操作成功");
    } else {
            messageContext.addError("msg", "任务未找到");
        }
    }

    /**
     * 查询某个表的数据并把子表数据打平
     * @param message
     * @param formId
     * @param dataId
     * @return
     */
    private Map getFormData(FormSubmitDataMessage message, String formId, String dataId){
        FormSubmitData palyload = message.getPalyload();
        palyload.setFormId(formId);
        palyload.setDataId(dataId);
        MessageContext tempMessageContext = new MessageContext();
        hissFormModelService.queryFormData(message,tempMessageContext);
        if(tempMessageContext.isSuccess()) {
            Map data = (Map) tempMessageContext.getResult().get("result");
            Map variables = new HashMap(); // 表单初始化变量
            dataToVariable(data, variables);
            return variables;
        }
        return null;
    }

    /**
     * 发起预备流程
     * @param params
     * @param messageContext
     */
    @Override
    public void sendPreProcess(FormSubmitDataMessage params, MessageContext messageContext) {
        FormSubmitData palyload = params.getPalyload();
        if(StrUtil.isNotEmpty(palyload.getModelId())){
            HissProcessPreLaunch launch = hissProcessPreLaunchMapper.selectById(palyload.getModelId());
            if(launch!=null){
                boolean admin = AdminUtil.isAdmin(params);
                if(admin || launch.getUserId().equals(params.getMessageAuth().getCurrentUser().getUserId())){
                    // 如果前端有变更的数据，需要重置到流程变量中
                    Map variables = new HashMap();
                    if(StrUtil.isNotEmpty(palyload.getFormId())){
                        String[] forms = palyload.getFormId().split(",");
                        List<HissProcessForm> hissProcessForms = hissProcessFormMapper.listFormByLaunchId(launch.getId());
                        if(hissProcessForms!=null){
                            for (HissProcessForm hissProcessForm : hissProcessForms) {
                                for (String formId : forms) {
                                    if(formId.equals(hissProcessForm.getFormId())){
                                        if(StrUtil.isNotEmpty(hissProcessForm.getDataId())){
                                            Map temp = getFormData(params, hissProcessForm.getFormId(),hissProcessForm.getDataId());
                                            if(temp!=null){
                                                variables.putAll(temp);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // 发起流程
                    ProcessDesignModel processDesignModel = new ProcessDesignModel();
                    processDesignModel.setModelId(launch.getModelId());
                    processDesignModel.setVariables(variables);
                    ProcessDesignModelMessage processDesignModelMessage = new ProcessDesignModelMessage();
                    processDesignModelMessage.setMessageAuth(params.getMessageAuth());
                    processDesignModelMessage.setPalyload(processDesignModel);
                    MessageContext tempMessageContext = new MessageContext();
                    startByModel(processDesignModelMessage,tempMessageContext,false);
                    if(tempMessageContext.isSuccess()){
                        String processInstanceId = (String) tempMessageContext.getResult().get("processInstanceId");
                        // 把预备表单的数据迁移到流程上
                        hissProcessFormMapper.updateLaunchToProcessInstance(launch.getId(),processInstanceId,launch.getTenantId());
                        // 删除预备流程信息
                        hissProcessPreLaunchMapper.deleteById(launch);
                        messageContext.addResultAndCount("processInstanceId", processInstanceId);
                    }else{
                        messageContext.setError(tempMessageContext.getError());
                    }
                }else{
                    messageContext.addError("msg","无权限操作");
                }
            }else{
                messageContext.addError("msg","未找到对应的预备流程");
            }
        }else{
            messageContext.addError("msg","未找到对应的预备流程");
        }
    }

    /**
     * 催办一个任务
     * @param params
     * @param messageContext
     */
    @Override
    public void expediteTask(TaskMessage params, MessageContext messageContext) {
        HissTask palyload = params.getPalyload();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(palyload.getProcessInstanceId()).singleResult();
        if (processInstance != null) {
            if(!params.getMessageAuth().getCurrentUser().getUserId().equals(processInstance.getStartUserId())){
                messageContext.addError("msg","无权限进行此操作");
                return;
            }
            List<Task> list = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).list();
            Set<String> userIds = new HashSet<>();
            if(list!=null){
                for (Task tk : list) {
                    if(StrUtil.isNotEmpty(tk.getAssignee())){
                        userIds.add(tk.getAssignee());
                    }
                }
            }
            if(userIds.size()>0){
                String tenantId = processInstance.getTenantId();
                HissActivitiEvent hissActivitiEvent = HissActivitiEventBuilder.builder()
                        .eventName(HissActivitiEventTypeEnum.USER_EXPEDITE.name())
                        .build();
                hissActivitiEvent.setEventData(userIds);
                hissActivitiEvent.setOperationType(EventOperationTypeEnum.GLOBAL_EVENT_NOTICE);
                MessageContext tempContext = hissServerApperanceTemplate.eventActivitiProcessNotice(tenantId, hissActivitiEvent);
                if(tempContext.isSuccess()){
                    messageContext.addResultAndCount("msg","操作成功！");
                }else{
                    messageContext.setError(tempContext.getError());
                }
            }else{
                messageContext.addError("msg","当前流程没有要催办的用户");
            }
        }else{
            messageContext.addError("msg","未找到对应流程");
        }
    }

    /**
     * 重新发起一个流程
     * @param params
     * @param messageContext
     */
    @Override
    public void restartProcess(TaskMessage params, MessageContext messageContext) {
        HissTask palyload = params.getPalyload();
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(palyload.getProcessInstanceId()).singleResult();
        if (processInstance != null) {
            if (!params.getMessageAuth().getCurrentUser().getUserId().equals(processInstance.getStartUserId())) {
                messageContext.addError("msg", "无权限进行此操作");
                return;
            }
            Model model = repositoryService.createModelQuery().deploymentId(processInstance.getDeploymentId()).modelTenantId(processInstance.getTenantId()).singleResult();
            if(model!=null){
                ProcessDesignModelMessage message = new ProcessDesignModelMessage();
                message.setMessageAuth(params.getMessageAuth());
                ProcessDesignModel processDesignModel = new ProcessDesignModel();
                processDesignModel.setModelId(model.getId());
                processDesignModel.setBusinessKey(processInstance.getBusinessKey());
                message.setPalyload(processDesignModel);
                MessageContext tempContext = new MessageContext();
                this.startByModel(message,tempContext,true);
                if(tempContext.isSuccess()){
                    String newProcessInstanceId = (String) tempContext.getResult().get("processInstanceId");
                    copyFormData(processInstance,newProcessInstanceId);
                    messageContext.addResultAndCount("processInstanceId",newProcessInstanceId);
                }else{
                    messageContext.setError(tempContext.getError());
                }
            }else{
                messageContext.addError("msg","未找到对应模型，不能重新发起流程");
            }
        }else{
            messageContext.addError("msg","未找到对应流程");
        }
    }

    /**
     * 复制老流程的表单数据到新流程中
     * @param processInstance
     * @param newProcessInstanceId
     */
    private void copyFormData(HistoricProcessInstance processInstance, String newProcessInstanceId) {
        List<HissProcessForm> oldFormList = hissProcessFormMapper.listFormByProcessInstanceId(processInstance.getId());
        List<HissProcessForm> newFormList = hissProcessFormMapper.listFormByProcessInstanceId(newProcessInstanceId);
        if(newFormList!=null && oldFormList!=null){
            for (HissProcessForm newForm : newFormList) {
                for (HissProcessForm oldForm : oldFormList) {
                    if(newForm.getFormId().equals(oldForm.getFormId())){
                        String oldDataId = oldForm.getDataId();
                        String newDataId = newForm.getDataId();
                        if(StrUtil.isNotEmpty(oldDataId) && StrUtil.isEmpty(newDataId)){
                            newDataId = UUID.randomUUID().toString();
                            hissFormModelService.copyFormData(oldForm.getFormId(),oldDataId,newDataId);
                            newForm.setDataId(newDataId);
                            hissProcessFormMapper.updateById(newForm);
                        }
                    }
                }
            }
        }
    }

    /**
     * 把表单的数据打平成流程的初始变量集合
     * @param data
     * @param variables
     */
    private void dataToVariable(Map data,Map variables) {
        if(data!=null){
            for (Object key : data.keySet()) {
                if(!variables.containsKey(key)){
                    Object value = data.get(key);
                    if(value instanceof Map){
                        dataToVariable((Map) value,variables);
                    }else{
                        variables.put(key,value);
                    }
                }
            }
        }
    }

}
