package cn.itcast.hiss.process.activiti.service;

import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.processdefinition.ProcessDesignModelMessage;

/**
 * 业务模式的实现
 *
 * @author: miukoo
 * @describe: activiti
 * @date: 2022/12/28 10:10
 */
public interface ActivitiDesignerService {

    /**
     * 保存文件
     * @param params
     * @return
     */
    public void saveBis(ProcessDesignModelMessage params, MessageContext messageContext);

    /**
     * 验证文件
     * @param params
     * @return
     */
    public void verification(ProcessDesignModelMessage params, MessageContext messageContext);

    void convertToDev(ProcessDesignModelMessage message, MessageContext messageContext);

    void saveDev(Message<ProcessDesignModelMessage> message, MessageContext messageContext);

    void get(Message<ProcessDesignModelMessage> message, MessageContext messageContext);
}
