package cn.itcast.hiss.process.activiti.handler.task;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.task.AnyJumpTask;
import cn.itcast.hiss.api.client.task.RejectType;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.task.AnyJumpTaskMessage;
import cn.itcast.hiss.process.activiti.service.ActivitiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Task_RollBackHandler
 *
 * @author: wgl
 * @describe: 任务驳回
 * 回退到指定节点
 * @date: 2022/12/28 10:10
 */
@Slf4j
@Component
public class Task_RollBackTaskHandler implements CmdHandler<AnyJumpTaskMessage> {

    @Autowired
    private ActivitiService activitiService;

    /**
     * 驳回功能实现
     *
     * @param params
     * @param messageContext
     */
    @Override
    public void invoke(Message params, MessageContext messageContext) {
        AnyJumpTask palyload = (AnyJumpTask) params.getPalyload();
        if (StrUtil.isEmpty(palyload.getOperatorName())) {
            palyload.setOperatorName("驳回");
        }
        //TODO 这里需要判断是否是有跳过环节节点
        if (palyload.getRejectType() == RejectType.IGNORE_ALL) {
            //如果是跳过环节节点，只需要记录开始节点和结束节点
            activitiService.saveSkipFlowNodes(palyload.getTaskId(), palyload.getJumpTaskOrActivityId(), params.getMessageAuth().getTenant());
        }
        if (palyload.getRejectType() == RejectType.SKIP_FLOW_NODES) {
            //如果是跳过环节节点，只需要记录开始节点和结束节点
            activitiService.saveHistoryTaskByTaskId(palyload.getTaskId(), palyload.getJumpTaskOrActivityId(), params.getMessageAuth().getTenant());
        }
        String jumpTaskOrActivityId = palyload.getJumpTaskOrActivityId();
        if ("#start".equals(jumpTaskOrActivityId)) {
            palyload.setJumpTaskOrActivityId(null);
            activitiService.rollBackTask((AnyJumpTaskMessage) params, messageContext, true);
        } else if ("#prev".equals(jumpTaskOrActivityId)) {
            palyload.setJumpTaskOrActivityId(null);
            activitiService.rollBackTask((AnyJumpTaskMessage) params, messageContext, false);
        } else if (StrUtil.isNotEmpty(jumpTaskOrActivityId)) {
            activitiService.rollBackTask((AnyJumpTaskMessage) params, messageContext, false);
        } else {
            messageContext.addError("msg", "缺失jumpTaskOrActivityId参数");
        }
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.TASK_ROLLBACK_TASK.getId();
    }


}
