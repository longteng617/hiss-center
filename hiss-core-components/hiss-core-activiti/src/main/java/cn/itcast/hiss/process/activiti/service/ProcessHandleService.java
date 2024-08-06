package cn.itcast.hiss.process.activiti.service;

import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sys.ProcessInstanceMessage;

/*
 * @author miukoo
 * @description 流程办理服务类
 * @date 2023/6/30 20:49
 * @version 1.0
 **/
public interface ProcessHandleService {

    /**
     * 查询申请人的待办、已办
     * @param message
     * @param messageContext
     */
    public void listProcessHandle(ProcessInstanceMessage message, MessageContext messageContext);

}
