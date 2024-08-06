package cn.itcast.hiss.process.activiti.util;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.HissProcessConstants;
import cn.itcast.hiss.api.client.common.AssignTypeEnum;
import cn.itcast.hiss.common.SystemConstant;
import cn.itcast.hiss.common.enums.ActiveNodeTypeEnum;
import cn.itcast.hiss.common.enums.ApprovalModeTypeEnum;
import cn.itcast.hiss.common.enums.InputType;
import cn.itcast.hiss.process.activiti.business.parse.BusinessConstants;
import cn.itcast.hiss.process.activiti.servicetask.ApprovalServiceTask;
import cn.itcast.hiss.process.activiti.servicetask.CcServiceTask;
import cn.itcast.hiss.process.activiti.servicetask.NotifierServiceTask;
import cn.itcast.hiss.process.activiti.servicetask.RejectServiceTask;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/*
 * @author miukoo
 * @description 把业务模式转成开发者模式
 * @date 2023/6/6 10:33
 * @version 1.0
 **/
public class ParseBusinessUtil {
    public static final Map<String,String>  CONDITION_SYMBOL = new HashMap<>();
    static {
        CONDITION_SYMBOL.put("eq","==");
        CONDITION_SYMBOL.put("lt","<");
        CONDITION_SYMBOL.put("lte","<=");
        CONDITION_SYMBOL.put("gt",">");
        CONDITION_SYMBOL.put("gte",">=");
        CONDITION_SYMBOL.put("in","");
        CONDITION_SYMBOL.put("notIn","!");
    }

    /**
     * 获取第一个开始节点
     * @param xml
     * @return
     * @throws Exception
     */
    public static String getFirstStarterId(String xml) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
        XMLInputFactory xif = XMLInputFactory.newInstance();
        InputStreamReader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        XMLStreamReader xtr = null;
        try {
            xtr = xif.createXMLStreamReader(in);
            BpmnModel bpmnModel = bpmnXMLConverter.convertToBpmnModel(xtr);
            Process mainProcess = bpmnModel.getMainProcess();
            Collection<FlowElement> flowElements = mainProcess.getFlowElements();
            for (FlowElement flowElement : flowElements) {
                if(flowElement instanceof UserTask){
                    UserTask userTask = (UserTask) flowElement;
                    String hissType = userTask.getAttributeValue(null, "hissType");
                    if(StrUtil.isNotEmpty(hissType)) {
                        if ("starter".equals(hissType)) {
                            return flowElement.getId();
                        }
                    }
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 把业务
     * @param configJson
     * @param xml
     * @return
     * @throws Exception
     */
    public static String getXml(String configJson,String xml) throws Exception {
        JSONObject configObject = JSONObject.parseObject(configJson);// 节点的设置对象
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
        XMLInputFactory xif = XMLInputFactory.newInstance();
        InputStreamReader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        XMLStreamReader xtr = null;
        xtr = xif.createXMLStreamReader(in);
        BpmnModel bpmnModel = bpmnXMLConverter.convertToBpmnModel(xtr);
        Process mainProcess = bpmnModel.getMainProcess();
        mainProcess.setExtensionElements(new HashMap<>());
        Map<String,Map<String,String>> buttonsConfig = new HashMap<>();
        List<Task> noOutElemenet = new ArrayList<>();
        int starterCount = 0;
        while (true){
            Collection<FlowElement> flowElements = mainProcess.getFlowElements();
            int count = 0;
            hiss:for (FlowElement flowElement : flowElements) {
                if(flowElement instanceof UserTask){
                    UserTask userTask = (UserTask) flowElement;
                    String hissType = userTask.getAttributeValue(null, "hissType");
                    if(StrUtil.isNotEmpty(hissType)) {
                        count++;
                        if ("starter".equals(hissType)) {
                            UserTask temp = parseStarter(userTask, mainProcess, bpmnModel, configObject);
                            if(temp.getOutgoingFlows().isEmpty()){
                                noOutElemenet.add(temp);
                            }
                            starterCount++;
                            break hiss;
                        }
                        if ("condition".equals(hissType)) {
                            parseCondition(userTask, mainProcess, bpmnModel,configObject);
                            break hiss;
                        }
                        if ("singleApprove".equals(hissType)) { // 单人审批
                            Task temp = parseSingleApprove(userTask, mainProcess, bpmnModel, configObject, buttonsConfig);
                            if(temp.getOutgoingFlows().isEmpty()){
                                noOutElemenet.add(temp);
                            }
                            break hiss;
                        }
                        if ("multipleApprove".equals(hissType)) { // 多人审批
                            UserTask temp = parseMultipleApprove(userTask, mainProcess, bpmnModel, configObject,buttonsConfig);
                            if(temp.getOutgoingFlows().isEmpty()){
                                noOutElemenet.add(temp);
                            }
                            break hiss;
                        }
                        if ("notifier".equals(hissType)) { // 知会
                            Task temp = parseCcOrNotifier(userTask, mainProcess, configObject,false);
                            if(temp.getOutgoingFlows().isEmpty()){
                                noOutElemenet.add(temp);
                            }
                            break hiss;
                        }
                        if ("cc".equals(hissType)) { // 抄送
                            Task temp = parseCcOrNotifier(userTask, mainProcess, configObject,true);
                            if(temp.getOutgoingFlows().isEmpty()){
                                noOutElemenet.add(temp);
                            }
                            break hiss;
                        }
                    }
                }
            }
            if(count==0){
                break;
            }
        }
        if(starterCount==0){
          throw new RuntimeException("流程必须要一个开始节点");
        }
        if(starterCount>1){
            throw new RuntimeException("流程必须只能有一个开始节点");
        }
        addEnd(mainProcess,bpmnModel,noOutElemenet);
        return new String(bpmnXMLConverter.convertToXML(bpmnModel));
    }

    /**
     * 解析知会和抄送
     * @param userTask
     * @param mainProcess
     * @param configObject
     * @param isCC
     * @return
     */
    private static ServiceTask parseCcOrNotifier(UserTask userTask, Process mainProcess, JSONObject configObject,boolean isCC) {
        JSONObject nodeConfig = configObject.getJSONObject("nodeConfig");
        JSONObject node = nodeConfig.getJSONObject(userTask.getId());

        ServiceTask newUserTask = new ServiceTask();
        newUserTask.setId(userTask.getId());
        newUserTask.setName(userTask.getName());
        newUserTask.setIncomingFlows(userTask.getIncomingFlows());
        newUserTask.setOutgoingFlows(userTask.getOutgoingFlows());
        newUserTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
        if(isCC){
            newUserTask.setImplementation(HissProcessConstants.INNER_CLASS_PREFIX+CcServiceTask.class.getName());
        }else{
            newUserTask.setImplementation(HissProcessConstants.INNER_CLASS_PREFIX+NotifierServiceTask.class.getName());
        }
        // 解析办理人的模式
        FieldExtension fieldExtension = new FieldExtension();
        JSONObject selectMode = node.getJSONObject("selectMode");
        if(selectMode!=null){
            String inputType = selectMode.getString("inputType");
            JSONObject target = selectMode.getJSONObject("target");
            JSONArray assignTypes = target.getJSONArray("assignType");
            String value =  getValue(target,1,true);
            if(value==null||value.length()==0){
                throw new RuntimeException("未给变量设置值【"+selectMode.getString("name")+"】~");
            }
            if(assignTypes==null||assignTypes.size()==0){
                assignTypes = new JSONArray(Arrays.asList(AssignTypeEnum.ASSIGN.name()));
            }
            String key = target.getString("key");// 自定义变量名
            if(assignTypes!=null&&assignTypes.size()>0){
                if(InputType.TREEQUERY.name().equalsIgnoreCase(inputType)
                        ||InputType.SELECTQUERY.name().equalsIgnoreCase(inputType)){
                    String longValue =  getValue(target,Integer.MAX_VALUE,true);
                    fieldExtension.setFieldName("users");
                    fieldExtension.setStringValue(longValue);
                }else{
                    value =  getValue(target,Integer.MAX_VALUE,false);
                    String temp = String.format(" ${hissVar.exchange('%s','%s')",key,value);
                    fieldExtension.setFieldName("expression");
                    fieldExtension.setExpression(temp);
                }
            }else{
                throw new RuntimeException("自定义【"+key+"】变量类型，未设置填充字段，请联系管理员~");
            }
        }else{
            throw new RuntimeException("未设置单人办理节点的办理人~");
        }
        newUserTask.setFieldExtensions(Arrays.asList(fieldExtension));
        mainProcess.removeFlowElement(userTask.getId());
        userTask = null;
        mainProcess.addFlowElement(newUserTask);
        return newUserTask;
    }

        /**
         * 添加一个结束节点
         * @param mainProcess
         * @param bpmnModel
         * @param noOutElemenet
         */
    private static void addEnd( Process mainProcess,BpmnModel bpmnModel,List<Task> noOutElemenet){
        if(noOutElemenet.isEmpty()){
            return;
        }
        Task userTask = noOutElemenet.get(0);
        GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(userTask.getId());
        // 添加开始节点和坐标
        GraphicInfo endGraphicInfo = new GraphicInfo();
        endGraphicInfo.setX(graphicInfo.getX()+graphicInfo.getWidth()+200);
        endGraphicInfo.setY(graphicInfo.getY()+graphicInfo.getHeight()/2-18);
        endGraphicInfo.setWidth(36);
        endGraphicInfo.setHeight(36);
        EndEvent endEvent = new EndEvent();
        endEvent.setId("Activity_"+UUID.randomUUID().toString().replace("-",""));

        endGraphicInfo.setElement(endEvent);
        bpmnModel.addGraphicInfo(endEvent.getId(),endGraphicInfo);
        // 连接开始节点和 发起人
        SequenceFlow sequenceFlow = new SequenceFlow();
        sequenceFlow.setId("Activity_"+UUID.randomUUID().toString().replace("-",""));
        sequenceFlow.setTargetRef(endEvent.getId());
        sequenceFlow.setSourceRef(userTask.getId());
        sequenceFlow.setTargetFlowElement(endEvent);
        sequenceFlow.setSourceFlowElement(userTask);
        // 设置两端
        List<SequenceFlow> incomingFlows = new ArrayList<>();
        incomingFlows.add(sequenceFlow);
        endEvent.setIncomingFlows(incomingFlows);
        List<SequenceFlow> outgoingFlows = userTask.getOutgoingFlows();
        if(outgoingFlows==null){
            outgoingFlows = new ArrayList<>();
        }
        outgoingFlows.add(sequenceFlow);
        userTask.setOutgoingFlows(outgoingFlows);
        List<GraphicInfo> gList = new ArrayList<>();
        GraphicInfo g2 = new GraphicInfo();
        g2.setX(graphicInfo.getX()+graphicInfo.getWidth());
        g2.setY(graphicInfo.getY()+graphicInfo.getHeight()/2);
        gList.add(g2);
        GraphicInfo g1 = new GraphicInfo();
        g1.setX(endGraphicInfo.getX());
        g1.setY(endGraphicInfo.getY()+endGraphicInfo.getHeight()/2);
        gList.add(g1);
        bpmnModel.addFlowGraphicInfoList(sequenceFlow.getId(),gList);
        if(noOutElemenet.size()>1){
            for (int i = 1; i < noOutElemenet.size(); i++) {
                Task temp = noOutElemenet.get(i);
                addEnd(mainProcess,bpmnModel,endEvent,temp);
            }
        }
        mainProcess.addFlowElement(endEvent);
        mainProcess.addFlowElement(sequenceFlow);

    }

    /**
     * 自动闭合结束节点
     * @param mainProcess
     * @param bpmnModel
     * @param endEvent
     * @param userTask
     */
    private static void addEnd( Process mainProcess,BpmnModel bpmnModel,EndEvent endEvent,Task userTask){
        // 连接开始节点和 发起人
        SequenceFlow sequenceFlow = new SequenceFlow();
        sequenceFlow.setId("Activity_"+UUID.randomUUID().toString().replace("-",""));
        sequenceFlow.setTargetRef(endEvent.getId());
        sequenceFlow.setSourceRef(userTask.getId());
        sequenceFlow.setTargetFlowElement(endEvent);
        sequenceFlow.setSourceFlowElement(userTask);
        // 设置两端
        List<SequenceFlow> incomingFlows = endEvent.getIncomingFlows();
        if(incomingFlows==null){
            incomingFlows = new ArrayList<>();
        }
        incomingFlows.add(sequenceFlow);
        endEvent.setIncomingFlows(incomingFlows);
        List<SequenceFlow> outgoingFlows = userTask.getOutgoingFlows();
        if(outgoingFlows==null){
            outgoingFlows = new ArrayList<>();
        }
        outgoingFlows.add(sequenceFlow);
        userTask.setOutgoingFlows(outgoingFlows);
        List<GraphicInfo> gList = new ArrayList<>();
        GraphicInfo endGraphicInfo = bpmnModel.getGraphicInfo(endEvent.getId());
        GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(userTask.getId());
        GraphicInfo g2 = new GraphicInfo();
        g2.setX(graphicInfo.getX()+graphicInfo.getWidth());
        g2.setY(graphicInfo.getY()+graphicInfo.getHeight()/2);
        gList.add(g2);
        GraphicInfo g1 = new GraphicInfo();
        g1.setX(endGraphicInfo.getX());
        g1.setY(endGraphicInfo.getY()+endGraphicInfo.getHeight()/2);
        gList.add(g1);
        bpmnModel.addFlowGraphicInfoList(sequenceFlow.getId(),gList);

        mainProcess.addFlowElement(sequenceFlow);
    }


    /**
     * 多人审批办理方式
     * @param userTask
     * @param mainProcess
     * @param bpmnModel
     * @param configObject
     * @param buttonsConfig
     */
    private static UserTask parseMultipleApprove(UserTask userTask, Process mainProcess, BpmnModel bpmnModel, JSONObject configObject ,Map<String,Map<String,String>> buttonsConfig) {
        JSONObject nodeConfig = configObject.getJSONObject("nodeConfig");
        JSONObject node = nodeConfig.getJSONObject(userTask.getId());

        UserTask newUserTask = new UserTask();
        newUserTask.setId(userTask.getId());
        newUserTask.setName(userTask.getName());
        newUserTask.setAssignee(userTask.getName());
        newUserTask.setCandidateUsers(userTask.getCandidateUsers());
        newUserTask.setCandidateGroups(userTask.getCandidateGroups());
        newUserTask.setIncomingFlows(userTask.getIncomingFlows());
        newUserTask.setOutgoingFlows(userTask.getOutgoingFlows());
        // 空节点处理模式,如果是
        String nullMode = node.getString(BusinessConstants.NULL_NODE_MODE);
        if(HissProcessConstants.TASK_VAR_NULL_NODE_AUTO_COMPLATE.equals(nullMode)){
            addDataObject(mainProcess,HissProcessConstants.getNullNodeAutoFlag(newUserTask.getId()),HissProcessConstants.TASK_VAR_AUTO_COMPLATE);
        }

        ExtensionAttribute type = new ExtensionAttribute(BpmnXMLConstants.ACTIVITI_EXTENSIONS_NAMESPACE,HissProcessConstants.NODE_TYPE);
        type.setValue(ActiveNodeTypeEnum.MULTIPLE_PARALLEL.name());
        // 多人办理模式
        String mode = node.getString(BusinessConstants.MULTIPLE_APPROVAL_MODE);
        MultiInstanceLoopCharacteristics multiInstance = new MultiInstanceLoopCharacteristics();
        multiInstance.setSequential(false);
        multiInstance.setElementVariable("a_ssignee_");
        multiInstance.setElementIndexVariable("a_ssignee_idx");
        newUserTask.setAssignee("${a_ssignee_}");
        if("sequential".equalsIgnoreCase(mode)){// 串签
            type.setValue(ActiveNodeTypeEnum.MULTIPLE_SEQUENTIAL.name());
            multiInstance.setSequential(true);
            multiInstance.setCompletionCondition("${nrOfCompletedInstances==nrOfInstances}");
        }else if("parallel".equalsIgnoreCase(mode)) {// 并签
            multiInstance.setCompletionCondition("${nrOfCompletedInstances==nrOfInstances}");
        }else if("or1".equalsIgnoreCase(mode)) {// 一签
            multiInstance.setCompletionCondition("${nrOfCompletedInstances==1}");
        }else if("or2".equalsIgnoreCase(mode)) {// 二签
            multiInstance.setCompletionCondition("${nrOfCompletedInstances==2||nrOfInstances<2}");
        }else if("or3".equalsIgnoreCase(mode)) {// 三签
            multiInstance.setCompletionCondition("${nrOfCompletedInstances==3||nrOfInstances<3}");
        }else if("or5".equalsIgnoreCase(mode)) {// 半签
            multiInstance.setCompletionCondition("${nrOfCompletedInstances/nrOfInstances > 0.5 }");
        }
        JSONObject selectMode = node.getJSONObject("selectMode");
        if(selectMode!=null){
            String inputType = selectMode.getString("inputType");
            JSONObject target = selectMode.getJSONObject("target");
            if(target==null){
                throw new RuntimeException("未给办理人对象【"+newUserTask.getName()+"】~");
            }
            String value =  getValue(target,1,true);
            if(value==null||value.length()==0){
                throw new RuntimeException("未给变量设置值【"+selectMode.getString("name")+"】~");
            }
            if(InputType.TREEQUERY.name().equalsIgnoreCase(inputType)
                    ||InputType.SELECTQUERY.name().equalsIgnoreCase(inputType)){
                String longValue =  getValue(target,Integer.MAX_VALUE,true);
                String temp = String.format(" ${hissVarLocal('%s')}",longValue);
                multiInstance.setInputDataItem(temp);
            }else{
                String key = target.getString("key");// 自定义变量名
                value =  getValue(target,Integer.MAX_VALUE,false);
                String temp = String.format(" ${hissVar.exchange('%s','%s')}",key,value);
                multiInstance.setInputDataItem(temp);
            }
        }else{
            throw new RuntimeException("未设置多人办理节点的办理人~");
        }
        newUserTask.addAttribute(type);
        newUserTask.setLoopCharacteristics(multiInstance);

        mainProcess.removeFlowElement(userTask.getId());
        userTask = null;
        mainProcess.addFlowElement(newUserTask);
        return newUserTask;
    }

    /**
     * 单人审批
     * @param userTask
     * @param mainProcess
     * @param bpmnModel
     * @param configObject
     */
    private static Task parseSingleApprove(UserTask userTask, Process mainProcess, BpmnModel bpmnModel,JSONObject configObject ,Map<String,Map<String,String>> buttonsConfig) {
        JSONObject nodeConfig = configObject.getJSONObject("nodeConfig");
        JSONObject node = nodeConfig.getJSONObject(userTask.getId());
        if(node==null){
            throw new RuntimeException("未设置单人办理节点的办理人~");
        }
        // 单人办理模式
        String mode = node.getString(BusinessConstants.SINGLE_APPROVAL_MODE);
        ApprovalModeTypeEnum approvalModeTypeEnum = ApprovalModeTypeEnum.MANUAL;
        if(StrUtil.isNotEmpty(mode)){
            approvalModeTypeEnum = ApprovalModeTypeEnum.stringToEnum(mode);
        }
        Task newUserTask = null;
        // 解析人工办理的模式
        if(approvalModeTypeEnum==ApprovalModeTypeEnum.MANUAL){
            UserTask uTask = new UserTask();
            uTask.setId(userTask.getId());
            uTask.setName(userTask.getName());
            uTask.setAssignee(userTask.getAssignee());
            uTask.setCandidateUsers(userTask.getCandidateUsers());
            uTask.setCandidateGroups(userTask.getCandidateGroups());
            uTask.setIncomingFlows(userTask.getIncomingFlows());
            uTask.setOutgoingFlows(userTask.getOutgoingFlows());
            ExtensionAttribute type = new ExtensionAttribute(BpmnXMLConstants.ACTIVITI_EXTENSIONS_NAMESPACE,HissProcessConstants.NODE_TYPE);
            type.setValue(ActiveNodeTypeEnum.SINGLE_APPROVE.name());
            uTask.addAttribute(type);
            parseUserInfo(approvalModeTypeEnum,uTask,null,node);
            newUserTask = uTask;
            // 空节点处理模式,如果是
            String nullMode = node.getString(BusinessConstants.NULL_NODE_MODE);
            if(HissProcessConstants.TASK_VAR_NULL_NODE_AUTO_COMPLATE.equals(nullMode)){
                addDataObject(mainProcess,HissProcessConstants.getNullNodeAutoFlag(newUserTask.getId()),HissProcessConstants.TASK_VAR_AUTO_COMPLATE);
            }
        }else{
            // 在发起流程时进行自动审核和自动提交
            ServiceTask serviceTask = new ServiceTask();
            serviceTask.setId(userTask.getId());
            serviceTask.setName(userTask.getName());
            serviceTask.setIncomingFlows(userTask.getIncomingFlows());
            serviceTask.setOutgoingFlows(userTask.getOutgoingFlows());
            serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
            if(approvalModeTypeEnum==ApprovalModeTypeEnum.APPROVAL){
                serviceTask.setImplementation(HissProcessConstants.INNER_CLASS_PREFIX+ ApprovalServiceTask.class.getName());
            }else{
                serviceTask.setImplementation(HissProcessConstants.INNER_CLASS_PREFIX+ RejectServiceTask.class.getName());
            }
            // 解析办理人的模式
            FieldExtension fieldExtension = new FieldExtension();
            parseUserInfo(approvalModeTypeEnum,null,fieldExtension,node);
            newUserTask = serviceTask;
        }
        mainProcess.removeFlowElement(userTask.getId());
        userTask = null;
        mainProcess.addFlowElement(newUserTask);
        return newUserTask;
    }

    /**
     * 解析里面的用户信息
     * @param newUserTask
     * @param node
     */
    private static void parseUserInfo(ApprovalModeTypeEnum approvalModeTypeEnum,UserTask newUserTask, FieldExtension fieldExtension,JSONObject node){
        JSONObject selectMode = node.getJSONObject("selectMode");
        if(selectMode!=null&&!selectMode.isEmpty()){
            String inputType = selectMode.getString("inputType");
            JSONObject target = selectMode.getJSONObject("target");
            JSONArray assignTypes = target.getJSONArray("assignType");
            String value =  getValue(target,1,true);
            // 无输入的，不用判断值
            if((value==null||value.length()==0)&&!InputType.NONE.name().equalsIgnoreCase(inputType)){
                throw new RuntimeException("未给变量设置值【"+selectMode.getString("name")+"】~");
            }
            if(assignTypes==null||assignTypes.size()==0){
                assignTypes = new JSONArray(Arrays.asList(AssignTypeEnum.ASSIGN.name()));
            }
            String key = target.getString("key");// 自定义变量名
            if(assignTypes!=null&&assignTypes.size()>0){
                if(InputType.TREEQUERY.name().equalsIgnoreCase(inputType)
                        ||InputType.SELECTQUERY.name().equalsIgnoreCase(inputType)){
                    String longValue =  getValue(target,Integer.MAX_VALUE,true);
                    if(newUserTask!=null){
                        setFieldValue(newUserTask,assignTypes,value,longValue);
                    }
                    if(fieldExtension!=null){
                        fieldExtension.setFieldName("users");
                        fieldExtension.setStringValue(longValue);
                    }
                }else{
                    value =  getValue(target,Integer.MAX_VALUE,false);
                    String temp = String.format(" ${hissVar.exchange('%s','%s')",key,value);
                    if(newUserTask!=null){
                        setFieldValue(newUserTask,assignTypes,temp,null);
                    }
                    if(fieldExtension!=null){
                        fieldExtension.setFieldName("expression");
                        fieldExtension.setExpression(temp);
                    }
                }
            }else{
                throw new RuntimeException("自定义【"+key+"】变量类型，未设置填充字段，请联系管理员~");
            }
        }else{
            if(approvalModeTypeEnum==ApprovalModeTypeEnum.MANUAL) {// 只有人工办理才抛错
                throw new RuntimeException("未设置单人办理节点的办理人~");
            }
        }
    }

    /**
     * 把值设置到对应的字段上
     * @param newUserTask
     * @param assignTypes
     * @param value
     */
    private static void setFieldValue(UserTask newUserTask,JSONArray assignTypes,String value,String longValue){
        for (int i = 0; i < assignTypes.size(); i++) {
            String assignType = assignTypes.getString(i);
            if(AssignTypeEnum.ASSIGN.name().equalsIgnoreCase(assignType)){
                newUserTask.setAssignee(value);
            }else if(AssignTypeEnum.CANDIDATE_USER.name().equalsIgnoreCase(assignType)){
                if(longValue!=null){
                    newUserTask.setCandidateUsers(Arrays.asList(longValue.split(",")));
                }else{
                    newUserTask.setCandidateUsers(Arrays.asList(value));
                }
            }else if(AssignTypeEnum.CANDIDATE_GROUP.name().equalsIgnoreCase(assignType)){
                if(longValue!=null){
                    newUserTask.setCandidateGroups(Arrays.asList(longValue.split(",")));
                }else{
                    newUserTask.setCandidateGroups(Arrays.asList(value));
                }
            }else{
                throw new RuntimeException("自定义的变量类型，未识别的类型【"+assignType+"】，请联系管理员~");
            }
        }
    }

    /**
     * 获取对象
     * @param target
     * @return
     */
    private static String getValue(JSONObject target,int limit,boolean isSemicolon){
        Object config = target.get("config");
        if(config==null){
            return null;
        }
        if(config instanceof JSONArray){
            JSONArray array = (JSONArray)config;
            StringJoiner stringJoiner = null;
            if(isSemicolon){
                stringJoiner = new StringJoiner(",");
            }else{
                stringJoiner = new StringJoiner("','");
            }
            int count=0;
            for (int i = 0; i < array.size(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                String id = jsonObject.getString("id");//第一个配置元素
                stringJoiner.add(id);
                count++;
                if(count>=limit){
                    break;
                }
            }
            return stringJoiner.toString();
        }else{
            return config.toString();
        }
    }


    /**
     * 把条件转成成表达式
     * @param userTask
     * @param mainProcess
     * @param bpmnModel
     * @return
     */
    public static void parseCondition(UserTask userTask, Process mainProcess, BpmnModel bpmnModel,JSONObject configObject){
        JSONObject nodeConfig = configObject.getJSONObject("nodeConfig");
        JSONObject node = nodeConfig.getJSONObject(userTask.getId());
        JSONArray formConfig = node.getJSONArray("formConfig");
        StringJoiner stringJoiner = new StringJoiner(" && ");
        if(formConfig!=null){
            for (int i = 0; i < formConfig.size(); i++) {
                JSONObject jsonObject = formConfig.getJSONObject(i);
                JSONArray fields = jsonObject.getJSONArray("fields");
                if(fields!=null){
                    for (int j = 0; j < fields.size(); j++) {
                        JSONObject field = fields.getJSONObject(j);
                        String name = field.getString("fieldName");
                        String condition = field.getString("condition");
                        String value1 = field.getString("conditionValue1");
                        String value2 = field.getString("conditionValue2");
                        conditionToExpression(stringJoiner,name,condition,value1,value2);
                    }
                }
            }
        }
        String condition = stringJoiner.toString();
        if(StrUtil.isNotEmpty(condition)){
            condition = String.format("${%s}",condition);
        }
        SequenceFlow sequenceFlow = new SequenceFlow();
        sequenceFlow.setId(userTask.getId());
        sequenceFlow.setName(userTask.getName());
        sequenceFlow.setConditionExpression(condition);
        List<GraphicInfo> points = new ArrayList<>();
        // 重新连线
        List<SequenceFlow> incomingFlows = userTask.getIncomingFlows();
        if(incomingFlows!=null){
            for (SequenceFlow incomingFlow : incomingFlows) {
                FlowElement sourceFlowElement = incomingFlow.getSourceFlowElement();
                sequenceFlow.setSourceFlowElement(sourceFlowElement);
                sequenceFlow.setSourceRef(sourceFlowElement.getId());
                if(sourceFlowElement instanceof UserTask){
                    UserTask sourceUserTask = (UserTask)sourceFlowElement;
                    List<SequenceFlow> tempOutgoingFlows = sourceUserTask.getOutgoingFlows();
                    tempOutgoingFlows.remove(incomingFlow);
                    tempOutgoingFlows.add(sequenceFlow);
                }
                // 获取线的起点
                List<GraphicInfo> flowLocationGraphicInfo = bpmnModel.getFlowLocationGraphicInfo(incomingFlow.getId());
                if(flowLocationGraphicInfo!=null){
                    points.add(flowLocationGraphicInfo.get(0));
                }
                mainProcess.removeFlowElement(incomingFlow.getId());
            }
        }
        List<SequenceFlow> outgoingFlows = userTask.getOutgoingFlows();
        if(outgoingFlows!=null){
            for (SequenceFlow outgoingFlow : outgoingFlows) {
                FlowElement targetFlowElement = outgoingFlow.getTargetFlowElement();
                sequenceFlow.setTargetFlowElement(targetFlowElement);
                sequenceFlow.setTargetRef(targetFlowElement.getId());
                if(targetFlowElement instanceof UserTask){
                    UserTask targetUserTask = (UserTask)targetFlowElement;
                    List<SequenceFlow> tempIncomingFlows = targetUserTask.getIncomingFlows();
                    tempIncomingFlows.remove(outgoingFlow);
                    tempIncomingFlows.add(sequenceFlow);
                }
                // 获取线的终点
                List<GraphicInfo> flowLocationGraphicInfo = bpmnModel.getFlowLocationGraphicInfo(outgoingFlow.getId());
                if(flowLocationGraphicInfo!=null){
                    points.add(flowLocationGraphicInfo.get(1));
                }
                mainProcess.removeFlowElement(outgoingFlow.getId());
            }
        }
        if(sequenceFlow.getSourceFlowElement()==null || sequenceFlow.getTargetFlowElement()==null) {
            throw new RuntimeException("条件表达式缺少一段的连线");
        }
        mainProcess.removeFlowElement(userTask.getId());
        bpmnModel.removeGraphicInfo(userTask.getId());
        userTask = null;
        bpmnModel.addFlowGraphicInfoList(sequenceFlow.getId(),points);
        mainProcess.addFlowElement(sequenceFlow);
    }

    /**
     * 把配置转成条件表达式
     * @param stringJoiner
     * @param name
     * @param condition
     * @param value1
     * @param value2
     */
    public static void conditionToExpression(StringJoiner stringJoiner,String name,String condition,String value1,String value2){
        if("gt".equalsIgnoreCase(condition)||"gte".equalsIgnoreCase(condition)
                ||"lt".equalsIgnoreCase(condition)||"lte".equalsIgnoreCase(condition)||"eq".equalsIgnoreCase(condition)){
            stringJoiner.add(String.format("hissUtil:getFormVariable(execution,'%s') %s %s",name,CONDITION_SYMBOL.get(condition),value1));
        }
        if("between".equalsIgnoreCase(condition)){
            stringJoiner.add(String.format("( hissUtil:getFormVariable(execution,'%s') >= %s",name,value1));
            stringJoiner.add(String.format("hissUtil:getFormVariable(execution,'%s') <= %s )",name,value2));
        }
        if("in".equalsIgnoreCase(condition)||"notIn".equalsIgnoreCase(condition)){// 不包含任、包含任意一个字符
            String[] split1 = value1.split(",");
            StringJoiner sj = new StringJoiner(",");
            for (String s : split1) {
                sj.add("\""+s+"\"");
            }
            String temp = sj.toString();
            if(temp.length()>0){
                temp=","+temp;
            }
            stringJoiner.add(String.format("%shissUtil:hissContainsAny(%s,'%s')",CONDITION_SYMBOL.get(condition),String.format("execution,'%s'",name),value1));
        }
    }

    /**
     * 解析发起人节点
     * @param userTask
     * @param mainProcess
     * @param bpmnModel
     * @param configObject
     */
    public static UserTask parseStarter(UserTask userTask, Process mainProcess, BpmnModel bpmnModel,JSONObject configObject){
        List<SequenceFlow> incomingFlows = userTask.getIncomingFlows();
        if(incomingFlows!=null&&incomingFlows.size()>0){
            throw new RuntimeException("发起人节点必须放在第一个节点位置上~");
        }
        UserTask newUserTask =new UserTask();
        ExtensionAttribute type = new ExtensionAttribute(BpmnXMLConstants.ACTIVITI_EXTENSIONS_NAMESPACE,HissProcessConstants.NODE_TYPE);
        type.setValue(ActiveNodeTypeEnum.STARTER.name());
        newUserTask.addAttribute(type);
        newUserTask.setId(userTask.getId());
        newUserTask.setName(userTask.getName());
        newUserTask.setOutgoingFlows(userTask.getOutgoingFlows());
        newUserTask.setIncomingFlows(userTask.getIncomingFlows());
        // 添加自动完成的标识
        addDataObject(mainProcess,HissProcessConstants.getAutoComplateFlag(newUserTask.getId()),HissProcessConstants.TASK_VAR_AUTO_COMPLATE);
        JSONObject nodeConfig = configObject.getJSONObject("nodeConfig");
        JSONObject node = nodeConfig.getJSONObject(userTask.getId());
        if(node==null){
            throw new RuntimeException("未找到发起人节点的配置，请检查后重试~");
        }
        String hissBusinessStarterUserType = node.getString(BusinessConstants.NODE_CONFING_STARTER_USER_TYPE);
        if("currentLoginUser".equals(hissBusinessStarterUserType)){// 当前登录人
            newUserTask.setAssignee(String.format("${%s}", SystemConstant.TASK_VARIABLES_CREATE_USERID));
        }
        if("fixedUser".equals(hissBusinessStarterUserType)){//固定发起人
            JSONArray jsonArray = node.getJSONArray(BusinessConstants.NODE_CONFING_STARTER_FIXED_USER);
            if(jsonArray!=null&&jsonArray.size()>0){
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                newUserTask.setAssignee(jsonObject.getString("id"));
            }else{
                throw new RuntimeException("没有设置对应的固定发起人~");
            }
        }
        // 可以发起的人员
        if(node.containsKey(BusinessConstants.NODE_CONFING_STARTER_USE_USER)){
            Object temp = node.get(BusinessConstants.NODE_CONFING_STARTER_USE_USER);
            if(temp instanceof JSONArray){
                JSONArray jsonArray = (JSONArray) temp;
                StringJoiner stringJoiner = new StringJoiner(",");
                int count =0;
                if(jsonArray!=null&&jsonArray.size()>0){
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String id = jsonObject.getString("id");
                    String name = jsonObject.getString("name");
                    if(!"所有人".equalsIgnoreCase(name)){
                        count++;
                    }
                    if(StrUtil.isNotEmpty(id)){
                        stringJoiner.add(id);
                    }
                }
                String users = stringJoiner.toString();
                if(StrUtil.isNotEmpty(users)){
                    setProcessProperty(mainProcess,HissProcessConstants.PROCESSDEFINITION_WHITE_LIST_USERS,users);
                }else{
                    if(count>0){
                        throw new RuntimeException("可以发起流程的人员限定设置了，但是解析为空~");
                    }
                }
            }
        }
        mainProcess.removeFlowElement(userTask.getId());
        userTask = null;
        mainProcess.addFlowElement(newUserTask);
        addStart(mainProcess,bpmnModel,newUserTask);
        return newUserTask;
    }

    /**
     * 添加一个流程属性
     * @param mainProcess
     * @param key
     * @param value
     */
    private static void setProcessProperty(Process mainProcess,String key,String value){
        Map<String, List<ExtensionElement>> emaps = mainProcess.getExtensionElements();
        List<ExtensionElement> list = emaps.get("properties");
        if(list==null){
            list = new ArrayList<>();
        }
        ExtensionElement property = new ExtensionElement();
        property.setName("property");
        property.setNamespace(BpmnXMLConstants.ACTIVITI_EXTENSIONS_NAMESPACE);
        ExtensionAttribute id = new ExtensionAttribute(BpmnXMLConstants.ACTIVITI_EXTENSIONS_NAMESPACE,"id");
        id.setValue( UUID.randomUUID().toString().replace("-", ""));
        property.addAttribute(id);
        ExtensionAttribute nameAttr = new ExtensionAttribute(BpmnXMLConstants.ACTIVITI_EXTENSIONS_NAMESPACE,"name");
        nameAttr.setValue(key);
        property.addAttribute(nameAttr);
        ExtensionAttribute valueAttr = new ExtensionAttribute(BpmnXMLConstants.ACTIVITI_EXTENSIONS_NAMESPACE,"value");
        valueAttr.setValue(value);
        property.addAttribute(valueAttr);
        list.add(property);
        emaps.put("properties",list);
        mainProcess.setExtensionElements(emaps);
    }

    /**
     * 流程的初始化变量
     * @param key
     * @param value
     */
    private static void addDataObject(Process mainProcess,String key,String value){
        ItemDefinition itemDefinition = new ItemDefinition();
        itemDefinition.setStructureRef("xsd:string");
        StringDataObject dataObject = new StringDataObject();
        dataObject.setName(key);
        dataObject.setValue(value);
        dataObject.setItemSubjectRef(itemDefinition);
        dataObject.setId("DO_"+UUID.randomUUID().toString().replace("-",""));
        mainProcess.addFlowElement(dataObject);
    }

    /**
     * 添加一个开始节点
     * @param mainProcess
     * @param bpmnModel
     * @param userTask
     */
    public static void addStart( Process mainProcess,BpmnModel bpmnModel,UserTask userTask){
        GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(userTask.getId());
        // 添加开始节点和坐标
        GraphicInfo startGraphicInfo = new GraphicInfo();
        startGraphicInfo.setX(graphicInfo.getX()-150);
        startGraphicInfo.setY(graphicInfo.getY()+ graphicInfo.getHeight()/2-18);
        startGraphicInfo.setWidth(36);
        startGraphicInfo.setHeight(36);
        StartEvent startEvent = new StartEvent();
        startEvent.setId("Activity_"+UUID.randomUUID().toString().replace("-",""));

        startGraphicInfo.setElement(startEvent);
        bpmnModel.addGraphicInfo(startEvent.getId(),startGraphicInfo);
        // 连接开始节点和 发起人
        SequenceFlow sequenceFlow = new SequenceFlow();
        sequenceFlow.setId("Activity_"+UUID.randomUUID().toString().replace("-",""));
        sequenceFlow.setTargetRef(userTask.getId());
        sequenceFlow.setSourceRef(startEvent.getId());
        sequenceFlow.setTargetFlowElement(userTask);
        sequenceFlow.setSourceFlowElement(startEvent);
        // 设置两端
        startEvent.setOutgoingFlows(Arrays.asList(sequenceFlow));
        List<SequenceFlow> incomingFlows = userTask.getIncomingFlows();
        if(incomingFlows==null){
            incomingFlows = new ArrayList<>();
        }
        incomingFlows.add(sequenceFlow);
        userTask.setIncomingFlows(incomingFlows);
        List<GraphicInfo> gList = new ArrayList<>();
        GraphicInfo g1 = new GraphicInfo();
        g1.setX(startGraphicInfo.getX()+ startGraphicInfo.getWidth());
        g1.setY(startGraphicInfo.getY()+startGraphicInfo.getHeight()/2);
        gList.add(g1);
        GraphicInfo g2 = new GraphicInfo();
        g2.setX(graphicInfo.getX());
        g2.setY(graphicInfo.getY()+graphicInfo.getHeight()/2);
        gList.add(g2);
        bpmnModel.addFlowGraphicInfoList(sequenceFlow.getId(),gList);

        mainProcess.addFlowElement(startEvent);
        mainProcess.addFlowElement(sequenceFlow);
    }

}
