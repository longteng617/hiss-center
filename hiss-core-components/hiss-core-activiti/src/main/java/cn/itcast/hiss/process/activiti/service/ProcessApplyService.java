package cn.itcast.hiss.process.activiti.service;

import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sys.ProcessInstanceMessage;

/*
 * @author miukoo
 * @description 流程发起人服务类
 * @date 2023/6/30 20:49
 * @version 1.0
 **/
public interface ProcessApplyService {

    /**
     * 查询申请人发起的数据列表
     * @param message
     * @param messageContext
     */
    public void listProcessApply(ProcessInstanceMessage message, MessageContext messageContext);

    void deleteProcessApply(ProcessInstanceMessage params, MessageContext messageContext);
}
