package com.itcast.activiti;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@Slf4j
public class ProcessHandleTest {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    /**
     * 完成填写清单  任务ID
     */
    @ParameterizedTest
    @ValueSource(strings = {"45e3a45f-4f27-11ef-8400-22af3702ff1d"})
    public void testCompleteNode2(String taskId){
        // 把张三填写的请假单中的数据，作为流程变量，设置到流程中
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName","张三");
        variables.put("startDate","2024-1-1");
        variables.put("days","1");
        variables.put("reason","元旦回家");
        // 标记任务完成
        taskService.complete(taskId, variables);
    }

    /**
     * 同意  任务ID
     */
    @ParameterizedTest
    @ValueSource(strings = {"32db9448-4f25-11ef-8c8f-22af3702ff1d"})
    public void testAgree(String taskId){
        // 把【同意】看做是填写的【审批表单（包括：审批结果、审批意见）】中的approvalStatus字段
        Map<String, Object> variables = new HashMap<>();
        variables.put("approvalStatus","同意");//
        variables.put("approvalNote","交接工作");
        // 标记任务完成
        taskService.complete(taskId, variables);
    }

    /**
     * 不同意，既拒绝  executionId
     */
    @ParameterizedTest
    @ValueSource(strings = {"45de261c-4f27-11ef-8400-22af3702ff1d"})
    public void testReject(String processInstanceId){
        String reason = "这里是不同意的理由";
        // 把【同意】看做是填写的【审批表单（包括：审批结果、审批意见）】中的approvalStatus字段
        Map<String, Object> variables = new HashMap<>();
        variables.put("approvalStatus","不同意");
        variables.put("approvalNote",reason);
        //记录流程变量
        runtimeService.setVariables(processInstanceId, variables);
        // 添加流程变量，表示任务被拒绝
        runtimeService.deleteProcessInstance(processInstanceId, reason);
    }

}

