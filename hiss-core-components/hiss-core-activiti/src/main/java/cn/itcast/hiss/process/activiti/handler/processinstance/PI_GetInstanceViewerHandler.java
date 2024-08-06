package cn.itcast.hiss.process.activiti.handler.processinstance;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.HissProcessConstants;
import cn.itcast.hiss.api.client.form.FormDefinitionInfo;
import cn.itcast.hiss.api.client.processdefinition.ProcessViewerModel;
import cn.itcast.hiss.api.client.processdefinition.viewer.*;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.common.dtos.Complex;
import cn.itcast.hiss.common.enums.*;
import cn.itcast.hiss.form.mapper.HissFormTableFieldsMapper;
import cn.itcast.hiss.form.pojo.HissFormTableFields;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.CurrentUser;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.processdefinition.ProcessViewerModelMessage;
import cn.itcast.hiss.process.activiti.mapper.HissProcessFormMapper;
import cn.itcast.hiss.process.activiti.mapper.HissProcessPreLaunchMapper;
import cn.itcast.hiss.process.activiti.mapper.HissSystemUserMapper;
import cn.itcast.hiss.process.activiti.pojo.HissProcessForm;
import cn.itcast.hiss.process.activiti.pojo.HissProcessPreLaunch;
import cn.itcast.hiss.process.activiti.pojo.HissSystemUser;
import cn.itcast.hiss.process.activiti.service.ActivitiService;
import cn.itcast.hiss.process.activiti.service.ClientUserService;
import cn.itcast.hiss.process.activiti.service.CommentService;
import cn.itcast.hiss.process.activiti.util.ParseBusinessUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableInstanceQuery;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 获取开发者模式流程图的执行过程
 *
 * @author: miukoo
 * @describe: 获取流程实例
 * @date: 2022/12/28 10:10
 */
@Slf4j
@Component
public class PI_GetInstanceViewerHandler implements CmdHandler<ProcessViewerModelMessage> {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private CommentService commentService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private HissProcessPreLaunchMapper hissProcessPreLaunchMapper;

    @Autowired
    private HissProcessFormMapper hissProcessFormMapper;

    @Autowired
    private ClientUserService clientUserService;

    @Autowired
    private HissSystemUserMapper hissSystemUserMapper;

    @Autowired
    private HissFormTableFieldsMapper hissFormTableFieldsMapper;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        ProcessViewerModel processViewerModel = (ProcessViewerModel)params.getPalyload();
        Complex<ProcessDefinition,String> complexProcessDefinition = getProcessDefinition(processViewerModel,params.getMessageAuth().getCurrentUser());
        HissSystemUser hissSystemUser = hissSystemUserMapper.selectByIdAndTenantId(params.getMessageAuth().getCurrentUser().getUserId(),params.getMessageAuth().getTenant());
        // 从已发起的流程信息中获取
        if(complexProcessDefinition!=null){
            ProcessDefinition processDefinition = complexProcessDefinition.getFirst();
            String processDefinitionId = processDefinition.getId();
            String processInstanceId = processViewerModel.getProcessInstanceId();
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
            // 获取流程的变量信息
            List<HistoricVariableInstance> lists = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();
            Map<String, Object> variables = new HashMap<>();
            if(lists!=null){
                for (HistoricVariableInstance inst : lists) {
                    variables.put(inst.getVariableName(),inst.getValue());
                }
            }
            // 获取流程评论信息
            List<ActHiComment> processInstanceComments = commentService.selectByProcessInstanceId(processInstanceId);
            Map<String,ActHiComment> comments = new HashMap<>();
            if(processInstanceComments!=null){
                for (ActHiComment item : processInstanceComments) {
                    if(!comments.containsKey(item.getTaskId())) {
                        comments.put(item.getTaskId(), item);
                    }
                }
                processViewerModel.setComments(processInstanceComments);
            }
            // 设置高亮数据,多实例把未完成的排在后面，因此按照完成时间排序
            List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().desc().list();
            Complex<Map<String, List<ViewerNodeInfo>>, List<ActiveNodeInfo>> complex = parseHighLightedData(bpmnModel, historicActivityInstances,variables,comments);
            processViewerModel.setHighlights(complex.getFirst());
            processViewerModel.setActiveTasks(complex.getSecond());
            addOtherActiveTask(bpmnModel,processInstanceId,processViewerModel.getActiveTasks(),variables);//把子任务信息添加到激活列表中
            List<String> deploymentResourceNames = repositoryService.getDeploymentResourceNames(processDefinition.getDeploymentId());
            // 设置元素文件内容
            InputStream inputStream = null;
            if(deploymentResourceNames.contains( HissProcessConstants.PROCESS_RUN_XML)){
                inputStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), HissProcessConstants.PROCESS_RUN_XML);
            }
            if(deploymentResourceNames.contains( HissProcessConstants.PROCESS_SHOW_XML)){
                inputStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), HissProcessConstants.PROCESS_SHOW_XML);
            }
            processViewerModel.setContent(IoUtil.readUtf8(inputStream));
            // 设置流程配置内容
            if(deploymentResourceNames.contains( HissProcessConstants.PROCESS_SHOW_CONFIG_JSON)){
                inputStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), HissProcessConstants.PROCESS_SHOW_CONFIG_JSON);
                if(inputStream!=null){
                    processViewerModel.setConfigJson(IoUtil.readUtf8(inputStream));
                }
            }
            // 设置节点信息
            processViewerModel.setNodes(parseGraphicList(bpmnModel.getMainProcess(),complex.getFirst()));
            // 设置流程状态基本信息
            processInfo(processViewerModel,processInstanceId,variables,hissSystemUser,deploymentResourceNames,complexProcessDefinition.getSecond());
            // 加载表单
            setProcessForm(complexProcessDefinition.getSecond(),processInstanceId,null,params.getMessageAuth().getTenant(),processViewerModel);

            // 写回数据
            messageContext.addResult("viewer",processViewerModel);
            return;
        }
        // 从流程预发起的列表中获取
        HissProcessPreLaunch hissProcessPreLaunch = hissProcessPreLaunchMapper.selectById(processViewerModel.getProcessInstanceId());
        if(hissProcessPreLaunch!=null){
            processViewerModel.setComments(new ArrayList<>());
            processViewerModel.setHighlights(new HashMap<>());
            processViewerModel.setActiveTasks(new ArrayList<>());
            Model model = repositoryService.getModel(hissProcessPreLaunch.getModelId());
            // 设置元素文件内容
            InputStream inputStream = repositoryService.getResourceAsStream(model.getDeploymentId(), HissProcessConstants.PROCESS_SHOW_XML);
            if(inputStream==null){
                inputStream = repositoryService.getResourceAsStream(model.getDeploymentId(), HissProcessConstants.PROCESS_RUN_XML);
            }
            processViewerModel.setContent(IoUtil.readUtf8(inputStream));
            // 设置流程配置内容
            inputStream = repositoryService.getResourceAsStream(model.getDeploymentId(), HissProcessConstants.PROCESS_SHOW_CONFIG_JSON);
            if(inputStream!=null){
                processViewerModel.setConfigJson(IoUtil.readUtf8(inputStream));
            }
            processViewerModel.setNodes(new ArrayList<>());
            ProcessInfo processInfo = new ProcessInfo();
            processInfo.setStartUserId(hissProcessPreLaunch.getUserId());
            processInfo.setName(hissProcessPreLaunch.getModelName());
            processInfo.setStatus(ProcessStatusEnum.PREPARE);
            processInfo.setAdmin(hissSystemUser!=null);
            processInfo.setTenantId(hissProcessPreLaunch.getTenantId());
            processInfo.setType(ModelTypeEnum.stringToEnum(model.getKey()));
            processInfo.setStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(hissProcessPreLaunch.getCreatedTime().getTime()), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            processViewerModel.setProcessInfo(processInfo);
            ActiveNodeInfo activeNodeInfo = new ActiveNodeInfo();
            activeNodeInfo.setNodeId(ParseBusinessUtil.getFirstStarterId(processViewerModel.getContent()));
            activeNodeInfo.setNodeType(ActiveNodeTypeEnum.STARTER);
            activeNodeInfo.setAssignee(hissProcessPreLaunch.getUserId());
            processViewerModel.setActiveTasks(Arrays.asList(activeNodeInfo));
            // 加载表单
            setProcessForm(null,null,""+hissProcessPreLaunch.getId(),hissProcessPreLaunch.getTenantId(),processViewerModel);
            // 写回数据
            messageContext.addResult("viewer",processViewerModel);
            return;
        }
        messageContext.addError("viewer","为找到对应的流程实例");
    }

    /**
     * 设置当前流程的表单信息
     * @param parentProcessInstanceId 如果有父流程，则从父流程中获取表单数据
     * @param processInstanceId
     * @param processViewerModel
     */
    private void setProcessForm(String parentProcessInstanceId,String processInstanceId,String launchId,String tenantId,ProcessViewerModel processViewerModel){
        String tempId = processInstanceId;
        if(StrUtil.isNotEmpty(parentProcessInstanceId)){
            tempId = parentProcessInstanceId;
        }
        LambdaQueryWrapper<HissProcessForm> wrapper = Wrappers.lambdaQuery();
        // 管理员，在流程中心查询，因此不用过滤
        if(!processViewerModel.getProcessInfo().getAdmin()){
            wrapper.eq(HissProcessForm::getTenantId,tenantId);
        }
        if(StrUtil.isNotEmpty(tempId)){
            wrapper.eq(HissProcessForm::getProcessInstanceId, tempId);
        }
        if(StrUtil.isNotEmpty(launchId)){
            wrapper.eq(HissProcessForm::getLaunchId, launchId);
        }
        List<HissProcessForm> hissProcessForms = hissProcessFormMapper.selectList(wrapper);
        // 知会和抄送时。需要加载字段
        if(StrUtil.isNotEmpty(processViewerModel.getProcessInfo().getParentProcessId()) || processViewerModel.getProcessInfo().getAdmin()){
            for (HissProcessForm hissProcessForm : hissProcessForms) {
                hissProcessForm.setFields(hissFormTableFieldsMapper.listByModelIdForProcessConfig(hissProcessForm.getFormId()));
            }
        }
        processViewerModel.setForms(hissProcessForms.stream().map(item->BeanUtil.copyProperties(item,ProcessFormInfo.class)).collect(Collectors.toList()));
    }

    /**
     * 获取知会、抄送等子任务信息
     * @param processInstanceId
     * @param activeNodeInfos
     */
    public void addOtherActiveTask(BpmnModel bpmnModel,String processInstanceId,List<ActiveNodeInfo> activeNodeInfos,Map<String, Object> variables){
        // 添加知会、抄送等子任务
        List<Task> list = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .executionId(null)
                .list();
        if(list!=null){
            for (Task task : list) {
                int count = 0;
                for (ActiveNodeInfo activeNodeInfo : activeNodeInfos) {
                    if(task.getId().equals(activeNodeInfo.getTaskId())){
                        count++;
                    }
                }
                if(count==0){
                    ActiveNodeInfo activeNodeInfo = new ActiveNodeInfo();
                    activeNodeInfo.setNodeType(parseMultiInstance(bpmnModel,task.getTaskDefinitionKey(),variables));
                    activeNodeInfo.setTaskId(task.getId());
                    activeNodeInfo.setActivityType("userTask");
                    activeNodeInfo.setAssignee(task.getAssignee());
                    activeNodeInfo.setNodeId(task.getTaskDefinitionKey());
                    activeNodeInfos.add(activeNodeInfo);
                    parseTaskCandidate(activeNodeInfo);
                }
            }
        }
    }

    public void processInfo(ProcessViewerModel processViewerModel,String processInstanceId,Map<String, Object> variables,HissSystemUser hissSystemUser,List<String> deploymentResourceNames,String parentProcessId){
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if(historicProcessInstance!=null){
            ProcessInfo processInfo = new ProcessInfo();
            if(historicProcessInstance.getStartTime()!=null){
                processInfo.setStatus(ProcessStatusEnum.ACTIVE);
                processInfo.setDuration(formatDuration(System.currentTimeMillis()-historicProcessInstance.getStartTime().getTime()));
                processInfo.setStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(historicProcessInstance.getStartTime().getTime()), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            if(historicProcessInstance.getEndTime()!=null){
                processInfo.setStatus(ProcessStatusEnum.COMPLETE);
                processInfo.setDuration(formatDuration(historicProcessInstance.getDurationInMillis()));
                processInfo.setEndTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(historicProcessInstance.getEndTime().getTime()), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            if(variables.containsKey(HissProcessConstants.PROCESS_STATUS)){
                String status = (String) variables.get(HissProcessConstants.PROCESS_STATUS);
                if(ProcessStatusEnum.CANCEL.name().equals(status)){
                    processInfo.setStatus(ProcessStatusEnum.CANCEL);
                }
            }
            // 只有 BIS模式才有
            if(!deploymentResourceNames.contains(HissProcessConstants.PROCESS_SHOW_XML)){
                processInfo.setType(ModelTypeEnum.DEV);
            }
            processInfo.setParentProcessId(parentProcessId);
            processInfo.setAdmin(hissSystemUser!=null);
            processInfo.setTenantId(historicProcessInstance.getTenantId());
            processInfo.setStartUserId(historicProcessInstance.getStartUserId());
            processInfo.setName(historicProcessInstance.getName());
            processInfo.setEndReason(historicProcessInstance.getDeleteReason());
            processViewerModel.setProcessInfo(processInfo);
        }
    }

    public static String formatDuration(long durationInMillis) {
        long days = TimeUnit.MILLISECONDS.toDays(durationInMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(durationInMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis) % 60;
        long milliseconds = durationInMillis % 1000;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天 ");
        }
        if (hours > 0) {
            sb.append(hours).append("小时 ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟 ");
        }
        if (seconds > 0) {
            sb.append(seconds).append("秒 ");
        }
        if (milliseconds > 0) {
            sb.append(milliseconds).append("毫秒 ");
        }

        return sb.toString().trim();
    }

    /**
     * 解析每个图节点的信息
     * @param process
     * @return
     */
    public List<GraphicNodeInfo> parseGraphicList(Process process,Map<String, List<ViewerNodeInfo>> map){
        List<GraphicNodeInfo> list = new ArrayList<>();
        for (FlowElement flowElement : process.getFlowElements()) {
            GraphicNodeInfo info = new GraphicNodeInfo();
            info.setId(flowElement.getId());
            info.setName(flowElement.getName());
            info.setActivityType(flowElement.getClass().getSimpleName());
            if(flowElement instanceof UserTask){
                UserTask userTask = (UserTask) flowElement;
                info.setAssignee(userTask.getAssignee());
            }
            List<ViewerNodeInfo> vs = map.get(flowElement.getId());
            if(vs!=null&&vs.size()>0){
                ViewerNodeInfo viewerNodeInfo = vs.get(0);
                info.setStatus(viewerNodeInfo.getStatus());
                info.setTaskId(viewerNodeInfo.getTaskId());
            }
            list.add(info);
        }
        return list;
    }

    /**
     * 解析悬浮框
     * @param process
     * @param viewerNodeInfo
     * @param historicActivityInstance
     * @param variables
     * @param comments
     */
    public void parseInfo(Process process,ViewerNodeInfo viewerNodeInfo,HistoricActivityInstance historicActivityInstance,Map<String, Object> variables,Map<String,ActHiComment> comments){
        String startTime = "";
        if(historicActivityInstance.getStartTime()!=null){
            startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(historicActivityInstance.getStartTime().getTime()), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        // 设置节点信息
        FlowElement flowElement = process.getFlowElement(historicActivityInstance.getActivityId());
        if(flowElement!=null){
            viewerNodeInfo.setNodeName(flowElement.getName());
        }
        viewerNodeInfo.setNodeId(historicActivityInstance.getActivityId());
        // 更多信息
        ViewerTipInfo tipInfo = new ViewerTipInfo();
        // ======================================== 处理批注 ==============================
        String assignee = historicActivityInstance.getAssignee();
        if(StrUtil.isNotEmpty(assignee)){
            viewerNodeInfo.setTipTitle("流程审批节点：");
            tipInfo.setUserId(assignee);
            tipInfo.setUserName(clientUserService.getUserName(historicActivityInstance.getTenantId(),assignee));
            tipInfo.setStartTime(startTime);
            if(viewerNodeInfo.getStatus()==ProcessNodeStatusEnum.COMPLETE){
                String endTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(historicActivityInstance.getEndTime().getTime()), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                tipInfo.setEndTime(endTime);
                tipInfo.setDuration(formatDuration(historicActivityInstance.getDurationInMillis()));
            }else{
                tipInfo.setDuration(formatDuration(System.currentTimeMillis()-historicActivityInstance.getStartTime().getTime()));
            }
        }
        // 标记节点为取消样式
        String isCancel = (String) variables.get(HissProcessConstants.PROCESS_STATUS);
        if(StrUtil.isNotEmpty(isCancel)){
            viewerNodeInfo.setStatus(ProcessNodeStatusEnum.CANCEL);
        }
        String activityType = historicActivityInstance.getActivityType();
        if(StrUtil.isNotEmpty(activityType)){
            if("startEvent".equalsIgnoreCase(activityType)||"exclusiveGateway".equalsIgnoreCase(activityType)
                    ||"parallelGateway".equals(activityType) || "inclusiveGateway".equals(activityType)){
                viewerNodeInfo.setTipTitle("开始时间");
                viewerNodeInfo.setTipContent(startTime);
                return;
            }
            if("endEvent".equalsIgnoreCase(activityType)){
                startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(historicActivityInstance.getEndTime().getTime()), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                viewerNodeInfo.setTipTitle("结束时间");
                viewerNodeInfo.setTipContent(startTime);
                return;
            }
        }

        // 拼接审批
        ActHiComment comment = comments.get(historicActivityInstance.getTaskId());
        if(comment!=null){
            if(StrUtil.isNotEmpty(comment.getMessage())){
                tipInfo.setOperateType(comment.getMessage());
            }
            tipInfo.setOperateNote(comment.getFullMsg());
        }
        // 删除原因
        String deleteReason = historicActivityInstance.getDeleteReason();
        if(StrUtil.isNotEmpty(deleteReason)){
            tipInfo.setOperateNote(deleteReason);
            viewerNodeInfo.setStatus(ProcessNodeStatusEnum.CANCEL);
        }
        viewerNodeInfo.setTipObject(tipInfo);
        log.error(historicActivityInstance.getActivityType()+"===============没有生成提示");
    }

    /**
     * 获取流程的高亮结果
     * @param bpmnModel
     * @param historicActInstances
     * @return
     */
    private Complex<Map<String,List<ViewerNodeInfo>>,List<ActiveNodeInfo>> parseHighLightedData(BpmnModel bpmnModel,List<HistoricActivityInstance> historicActInstances,Map<String, Object> variables,Map<String,ActHiComment> comments) {
        Process process = bpmnModel.getMainProcess();
        // 全部活动节点
        List<FlowNode> historicActivityNodes = new ArrayList<>();
        // 已完成的历史活动节点
        List<HistoricActivityInstance> finishedActivityInstances = new ArrayList<>();
        // 记录已经办理的流程节点和
        Map<String, List<ViewerNodeInfo>> result = new HashMap<>();
        // 记录当前需要处理的任务，
        List<ActiveNodeInfo> activeTask = new ArrayList<>();

        for (HistoricActivityInstance historicActivityInstance : historicActInstances) {
            FlowNode flowNode = (FlowNode) process.getFlowElement(historicActivityInstance.getActivityId(), true);
            historicActivityNodes.add(flowNode);
            List<ViewerNodeInfo> list = result.getOrDefault(flowNode.getId(), new ArrayList<ViewerNodeInfo>());
            ViewerNodeInfo viewerNodeInfo = new ViewerNodeInfo();
            viewerNodeInfo.setActivityType(historicActivityInstance.getActivityType());
            viewerNodeInfo.setTaskId(historicActivityInstance.getTaskId());
            viewerNodeInfo.setAssignee(historicActivityInstance.getAssignee());
            if (historicActivityInstance.getEndTime() != null) {
                finishedActivityInstances.add(historicActivityInstance);
                viewerNodeInfo.setStatus(ProcessNodeStatusEnum.COMPLETE);
                parseInfo(process,viewerNodeInfo,historicActivityInstance,variables,comments);
                list.add(viewerNodeInfo);
            } else {
                ActiveNodeInfo activeNodeInfo = new ActiveNodeInfo();
                activeNodeInfo.setActivityType(historicActivityInstance.getActivityType());
                activeNodeInfo.setAssignee(historicActivityInstance.getAssignee());
                activeNodeInfo.setTaskId(historicActivityInstance.getTaskId());
                activeNodeInfo.setNodeId(historicActivityInstance.getActivityId());
                activeNodeInfo.setNodeType(parseMultiInstance(bpmnModel,historicActivityInstance.getActivityId(),variables));
                activeTask.add(activeNodeInfo);
                parseTaskCandidate(activeNodeInfo);
                viewerNodeInfo.setStatus(ProcessNodeStatusEnum.ACTIVE);
                parseInfo(process,viewerNodeInfo,historicActivityInstance,variables,comments);
                list.add(0,viewerNodeInfo);
            }
            result.put(flowNode.getId(),list);
        }

        FlowNode currentFlowNode = null;
        FlowNode targetFlowNode = null;
        // 标记节点为取消样式
        String isCancel = (String) variables.get(HissProcessConstants.PROCESS_STATUS);
        // 遍历已完成的活动实例，从每个实例的outgoingFlows中找到已执行的
        for (HistoricActivityInstance currentActivityInstance : finishedActivityInstances) {
            // 获得当前活动对应的节点信息及outgoingFlows信息
            currentFlowNode = (FlowNode) process.getFlowElement(currentActivityInstance.getActivityId(), true);
            List<SequenceFlow> sequenceFlows = currentFlowNode.getOutgoingFlows();

            /**
             * 遍历outgoingFlows并找到已已流转的 满足如下条件认为已已流转：
             * 1.当前节点是并行网关或兼容网关，则通过outgoingFlows能够在历史活动中找到的全部节点均为已流转
             * 2.当前节点是以上两种类型之外的，通过outgoingFlows查找到的时间最早的流转节点视为有效流转
             */
            if ("exclusiveGateway".equalsIgnoreCase(currentActivityInstance.getActivityType())||"parallelGateway".equals(currentActivityInstance.getActivityType()) || "inclusiveGateway".equals(currentActivityInstance.getActivityType())) {
                // 遍历历史活动节点，找到匹配流程目标节点的
                for (SequenceFlow sequenceFlow : sequenceFlows) {
                    targetFlowNode = (FlowNode) process.getFlowElement(sequenceFlow.getTargetRef(), true);
                    if (historicActivityNodes.contains(targetFlowNode)) {
                        ViewerNodeInfo viewerNodeInfo = new ViewerNodeInfo();
                        viewerNodeInfo.setStatus(ProcessNodeStatusEnum.COMPLETE);
                        List<ViewerNodeInfo> list = result.getOrDefault(sequenceFlow.getId(), new ArrayList<ViewerNodeInfo>());
                        list.add(viewerNodeInfo);
                        result.put(sequenceFlow.getId(),list);
                    }
                }
            } else {
                List<Map<String, Object>> tempMapList = new ArrayList<>();
                for (SequenceFlow sequenceFlow : sequenceFlows) {
                    for (HistoricActivityInstance historicActivityInstance : historicActInstances) {
                        if (historicActivityInstance.getActivityId().equals(sequenceFlow.getTargetRef())) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("highLightedFlowId", sequenceFlow.getId());
                            map.put("highLightedFlowStartTime", historicActivityInstance.getStartTime().getTime());
                            tempMapList.add(map);
                        }
                    }
                }

                if (!CollectionUtils.isEmpty(tempMapList)) {
                    // 遍历匹配的集合，取得开始时间最早的一个
                    long earliestStamp = 0L;
                    String highLightedFlowId = null;
                    for (Map<String, Object> map : tempMapList) {
                        long highLightedFlowStartTime = Long.parseLong(map.get("highLightedFlowStartTime").toString());
                        if (earliestStamp == 0 || earliestStamp >= highLightedFlowStartTime) {
                            highLightedFlowId = map.get("highLightedFlowId").toString();
                            earliestStamp = highLightedFlowStartTime;
                        }
                    }
                    ViewerNodeInfo viewerNodeInfo = new ViewerNodeInfo();
                    if(StrUtil.isNotEmpty(isCancel)){
                        viewerNodeInfo.setStatus(ProcessNodeStatusEnum.CANCEL);
                    }else{
                        viewerNodeInfo.setStatus(ProcessNodeStatusEnum.COMPLETE);
                    }
                    List<ViewerNodeInfo> list = result.getOrDefault(highLightedFlowId, new ArrayList<ViewerNodeInfo>());
                    list.add(viewerNodeInfo);
                    result.put(highLightedFlowId,list);
                }
            }
        }

        return new Complex<Map<String,List<ViewerNodeInfo>>,List<ActiveNodeInfo>>(result,activeTask);
    }

    /**
     * 获取候选人
     * @param activeNodeInfo
     */
    private void parseTaskCandidate(ActiveNodeInfo activeNodeInfo){
        if(StrUtil.isNotEmpty(activeNodeInfo.getTaskId())) {
            try {
                List<IdentityLink> list = taskService.getIdentityLinksForTask(activeNodeInfo.getTaskId());
                activeNodeInfo.setCandidateUser(list.stream().filter(item->item.getType().equals("candidate")).collect(Collectors.toMap(IdentityLink::getUserId, IdentityLink::getType, (oldValue, newValue) -> newValue)));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 解析一个节点的多实例类型
     * @param bpmnModel
     * @param activityId
     * @return
     */
    private ActiveNodeTypeEnum parseMultiInstance(BpmnModel bpmnModel, String activityId,Map<String,Object> variables){
        FlowNode flowElement = (FlowNode) bpmnModel.getFlowElement(activityId);
        Object behavior = flowElement.getBehavior();
        if(behavior instanceof ParallelMultiInstanceBehavior){
            return ActiveNodeTypeEnum.MULTIPLE_PARALLEL;
        }
        if(behavior instanceof SequentialMultiInstanceBehavior){
            return ActiveNodeTypeEnum.MULTIPLE_SEQUENTIAL;
        }
        // 知会和抄送节点判断
        if(variables.containsKey(HissProcessConstants.NODE_TYPE)){
            String typeName = (String) variables.get(HissProcessConstants.NODE_TYPE);
            if(StrUtil.isNotEmpty(typeName)){
                return ActiveNodeTypeEnum.valueOf(typeName);
            }
        }
        // 多人、开始、单人节点的识别
        if(flowElement instanceof UserTask){
            UserTask userTask = (UserTask) flowElement;
            String nodeType = userTask.getAttributeValue(BpmnXMLConstants.ACTIVITI_EXTENSIONS_NAMESPACE, HissProcessConstants.NODE_TYPE);
            if(StrUtil.isNotEmpty(nodeType)){
                return ActiveNodeTypeEnum.valueOf(nodeType);
            }
            return ActiveNodeTypeEnum.SINGLE_APPROVE;
        }
        return ActiveNodeTypeEnum.NONE;
    }

    /**
     * 获取流程定义
     * @param processViewerModel
     * @return
     */
    private Complex<ProcessDefinition,String> getProcessDefinition(ProcessViewerModel processViewerModel, CurrentUser currentUser){
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processViewerModel.getProcessInstanceId())
                .singleResult();
        String processDefinitionId = "";
        String parentProcessInstance="";
        // 从历史表中去查询
        if(processInstance==null){
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processViewerModel.getProcessInstanceId()).singleResult();
            if(historicProcessInstance!=null){
                processDefinitionId = historicProcessInstance.getProcessDefinitionId();
                List<HistoricVariableInstance> list = historyService.createHistoricVariableInstanceQuery().processInstanceId(historicProcessInstance.getId()).list();
                if(list!=null){
                    for (HistoricVariableInstance historicVariableInstance : list) {
                        if(historicVariableInstance.getVariableName().equalsIgnoreCase(HissProcessConstants.PARENT_PROCESS_INSTANCE)){
                            parentProcessInstance = (String) historicVariableInstance.getValue();
                        }
                    }
                }
            }
        }else{
            Map<String, Object> processVariables = runtimeService.getVariables(processInstance.getProcessInstanceId());
            // 如果是知会和抄送，则从主流程中获取表单数据
            if(processVariables!=null&&processVariables.containsKey(HissProcessConstants.PARENT_PROCESS_INSTANCE)){
                parentProcessInstance = (String) processVariables.get(HissProcessConstants.PARENT_PROCESS_INSTANCE);
                // 如果是抄送，则打开既自动阅读
                String type = (String) processVariables.get(HissProcessConstants.NODE_TYPE);
                if(HissTaskTypeEnum.CC.name().equals(type)){
                    autoComplateCCRead(processInstance,currentUser);
                }
            }
            processDefinitionId = processInstance.getProcessDefinitionId();
        }
        if(StrUtil.isNotEmpty(processDefinitionId)){
            return new Complex<>(repositoryService.getProcessDefinition(processDefinitionId),parentProcessInstance);
        }
        return null;
    }

    /**
     * 自动阅读抄送的信息
     * @param processInstance
     * @param currentUser
     */
    private void autoComplateCCRead(ProcessInstance processInstance, CurrentUser currentUser){
        List<Task> list = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskTenantId(processInstance.getTenantId()).list();
        if(list!=null){
            for (Task task : list) {
                if(currentUser.getUserId().equals(task.getAssignee())) {
                    // 完成
                    taskService.complete(task.getId());
                    // 添加评论
                    String comment = String.format("%s【阅读】", currentUser.getUserName());
                    commentService.addComment(task, currentUser.getUserId(), comment, "");
                }
            }
        }
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.PI_GET_INSTANCE_DEV_VIEWER.getId();
    }
}
