package cn.itcast.hiss.process.activiti.handler.sys.process.instance;

import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sys.ProcessInstanceMessage;
import cn.itcast.hiss.message.sys.pojo.ProcessInstance;
import cn.itcast.hiss.process.activiti.mapper.HissUserAppMapper;
import cn.itcast.hiss.process.activiti.service.ProcessInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 *
 * @author: miukoo
 * @describe: 流程实例数据查询
 * @date: 2022/12/28 10:10
 */
@Component
@Slf4j
public class SYS_ProcessInstanceListHandler implements CmdHandler<ProcessInstanceMessage> {
    @Autowired
    private HissUserAppMapper hissUserAppMapper;
    @Autowired
    private ProcessInstanceService processInstanceService;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        ProcessInstance palyload = (ProcessInstance)params.getPalyload();
        boolean canQuery = isAdmin(params);
        if(!canQuery){
            List<String> userAppIdsList = hissUserAppMapper.getUserAppIdsList(params.getMessageAuth().getCurrentUser().getUserId());
            canQuery = userAppIdsList.contains(palyload.getTenantId());
        }
        if(canQuery) {
            processInstanceService.listProcessInstance((ProcessInstanceMessage) params, messageContext);
        }else{
            messageContext.addError("msg","无权限操作");
        }
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.SYS_PROCESS_INSTANCE_LIST.getId();
    }
}
