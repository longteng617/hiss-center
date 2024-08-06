package cn.itcast.hiss.process.activiti.servicetask;

import cn.hutool.extra.spring.SpringUtil;
import cn.itcast.hiss.process.activiti.service.ActivitiService;
import lombok.Data;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.FixedValue;

/*
 * @author miukoo
 * @description 自动拒绝
 * @date 2023/6/9 10:01
 * @version 1.0
 **/
@Data
public class RejectServiceTask implements JavaDelegate {

    // 表达式，需要执行
    Expression expression;
    // 固定人员列表
    FixedValue users;

    private static ActivitiService activitiService = SpringUtil.getBean(ActivitiService.class);
    private static TaskService taskService = SpringUtil.getBean(TaskService.class);
    @Override
    public void execute(DelegateExecution execution) {
        FlowElement currentFlowElement = execution.getCurrentFlowElement();
        if(currentFlowElement instanceof ServiceTask){
            String temp = null;
            if(users!=null){
                temp = users.getExpressionText();
            }
            activitiService.autoRejectTask(execution,temp,expression);
        }else{
            throw new RuntimeException("当前节点不是ServiceTask");
        }
    }
}
