package cn.itcast.hiss.process.activiti.service;

import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sys.ProcessInstanceMessage;
import cn.itcast.hiss.message.sys.pojo.ProcessInstance;

/*
 * @author miukoo
 * @description 流程实例服务类
 * @date 2023/6/30 20:49
 * @version 1.0
 **/
public interface ProcessInstanceService {

    /**
     * 查询数据列表
     * @param message
     * @param messageContext
     */
    public void listProcessInstance(ProcessInstanceMessage message, MessageContext messageContext);

    void deleteProcessInstance(ProcessInstanceMessage params, MessageContext messageContext);

    public void deleteHiProcessInstance(MessageContext messageContext, ProcessInstance palyload);
}
