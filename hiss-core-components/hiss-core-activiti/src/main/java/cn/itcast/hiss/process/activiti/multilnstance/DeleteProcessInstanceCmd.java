package cn.itcast.hiss.process.activiti.multilnstance;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

import java.io.Serializable;

/*
 * @author miukoo
 * @description 删除流程实例
 * @date 2023/7/2 17:50
 * @version 1.0
 **/
public class DeleteProcessInstanceCmd implements Command<Void>, Serializable {

    private static final long serialVersionUID = 1L;

    protected String processInstanceId;

    public DeleteProcessInstanceCmd(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        if (processInstanceId == null) {
            throw new ActivitiIllegalArgumentException("processInstanceId is null");
        }

        commandContext.getHistoricProcessInstanceEntityManager().delete(processInstanceId);

        return null;
    }

}
