package cn.itcast.hiss.process.activiti.handler.form;

import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.form.service.HissFormModelService;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.form.FormDataListMessage;
import cn.itcast.hiss.process.activiti.service.FormManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author: miukoo
 * @describe: 表单数据删除
 * @date: 2022/12/28 10:10
 */
@Component
public class Form_FormDataDeleteHandler implements CmdHandler<FormDataListMessage> {

    @Autowired
    private FormManagerService formManagerService;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        formManagerService.deleteFormData((FormDataListMessage)params,messageContext);
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.FORM_GET_MY_FORM_DATA_DELETE.getId();
    }
}
