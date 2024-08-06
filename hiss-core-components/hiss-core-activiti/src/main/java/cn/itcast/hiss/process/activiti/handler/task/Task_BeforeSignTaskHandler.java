package cn.itcast.hiss.process.activiti.handler.task;

import cn.hutool.core.util.ObjectUtil;
import cn.itcast.hiss.api.client.task.AddExecutionTask;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.task.AddExecutionTaskMessage;
import cn.itcast.hiss.process.activiti.multilnstance.AddSequenceMultiInstanceCmd;
import cn.itcast.hiss.process.activiti.service.ActivitiService;
import cn.itcast.hiss.process.activiti.vo.MultiInstanceVo;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Task_BeforeSignTaskHandler
 *
 * @author: wgl
 * @describe: 前加签任务处理器
 * @date: 2022/12/28 10:10
 */
@Component
public class Task_BeforeSignTaskHandler implements CmdHandler<AddExecutionTaskMessage> {

    @Autowired
    private TaskService taskService;
    @Autowired
    private ActivitiService activitiService;
    @Autowired
    private ManagementService managementService;
    @Autowired
    private RuntimeService runtimeService;


    @Override
    public void invoke(Message params, MessageContext messageContext) {
        AddExecutionTask addExecutionTask = (AddExecutionTask) params.getPalyload();
        Task task = taskService.createTaskQuery().taskId(addExecutionTask.getTaskId())
                .taskTenantId(params.getMessageAuth().getTenant())
                .taskCandidateOrAssigned(params.getMessageAuth().getCurrentUser().getUserId()).
                singleResult();
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
        MultiInstanceVo multiInstanceVo = activitiService.isMultiInstance(processDefinitionId, taskDefinitionKey);
        if (ObjectUtil.isEmpty(multiInstanceVo)) {
            messageContext.setResult(new ConcurrentHashMap<>() {
                {
                    put("msg", "当前环节不是会签节点");
                }
            });
        }
        try {
            //如果是并行多实例节点，则直不允许加签
            if (multiInstanceVo.getType() instanceof ParallelMultiInstanceBehavior) {
                //如果是并行多实例节点，则直接抛出异常
                messageContext.setResult(new ConcurrentHashMap<>() {
                    {
                        put("msg", "并行多实例节点不允许进行前加签");
                    }
                });
                return;
            } else if (multiInstanceVo.getType() instanceof SequentialMultiInstanceBehavior) {
                //如果是串行多实例节点，则需要先完成当前任务，再加签
                AddSequenceMultiInstanceCmd addSequenceMultiInstanceCmd = new AddSequenceMultiInstanceCmd(task.getId(),runtimeService.getVariables(task.getExecutionId()),addExecutionTask.getUserId(),addExecutionTask.getUserName(),taskService,true);
                managementService.executeCommand(addSequenceMultiInstanceCmd);
            }
            String userName = params.getMessageAuth().getCurrentUser().getUserName();
            taskService.addComment(task.getId(), processInstanceId, userName + "加签【" + String.join(",", addExecutionTask.getUserName()) + "】");
            taskService.complete(task.getId(),addExecutionTask.getVariables());
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

    @Override
    public String getId() {
       return HandlerIdClientEnum.TASK_BEFORE_SIGN_TASK.getId();
    }
}
