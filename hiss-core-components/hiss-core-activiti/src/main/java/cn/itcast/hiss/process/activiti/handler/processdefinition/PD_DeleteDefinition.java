package cn.itcast.hiss.process.activiti.handler.processdefinition;

import cn.itcast.hiss.api.client.processdefinition.DeleteProcessDefinition;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.processdefinition.DeleteProcessDefinitionMessage;
import org.activiti.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

;

/**
 * DelDefinition
 *
 * @author: wgl
 * @describe: 删除流程定义
 * @date: 2022/12/28 10:10
 */
@Component
public class PD_DeleteDefinition implements CmdHandler<DeleteProcessDefinitionMessage> {

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        DeleteProcessDefinition palyload = (DeleteProcessDefinition) params.getPalyload();
        repositoryService.deleteDeployment(palyload.getProcessID(), palyload.isHistoryDelFlag());
        messageContext.setResult(new ConcurrentHashMap() {
            {
                put("data", palyload.getProcessID());
                put("msg", "删除成功");
            }
        });
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.PD_DELETE_DEFINITION.getId();
    }
}
