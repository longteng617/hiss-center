package cn.itcast.hiss.process.activiti.handler.form;

import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.form.service.HissFormModelService;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.form.FormSubmitDataMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author: miukoo
 * @describe: 查询自动填充的字段数据
 * @date: 2022/12/28 10:10
 */
@Component
public class Form_QueryAutoFillDataHandler implements CmdHandler<FormSubmitDataMessage> {

    @Autowired
    private HissFormModelService hissFormModelService;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        try{
            hissFormModelService.queryAutoFillData((FormSubmitDataMessage)params,messageContext);
        }catch (Exception e){
            e.printStackTrace();
            messageContext.addError("msg","服务器处理错误："+e.getMessage());
        }
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.FORM_GET_MY_FORM_AUTO_FILL_DATA.getId();
    }
}
