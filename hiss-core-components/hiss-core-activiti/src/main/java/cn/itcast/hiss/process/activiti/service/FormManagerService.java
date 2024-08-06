package cn.itcast.hiss.process.activiti.service;

import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.form.FormDataListMessage;

/*
 * @author miukoo
 * @description 表单管理的CRUD
 * @date 2023/7/10 20:27
 * @version 1.0
 **/
public interface FormManagerService {
    void deleteFormData(FormDataListMessage params, MessageContext messageContext);

    void queryFormDataList(FormDataListMessage params, MessageContext messageContext);
}
