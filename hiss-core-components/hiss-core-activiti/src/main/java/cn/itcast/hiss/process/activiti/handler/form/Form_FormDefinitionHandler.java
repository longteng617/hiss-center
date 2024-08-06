package cn.itcast.hiss.process.activiti.handler.form;

import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.form.service.HissFormModelService;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.form.CreateFormDefinitionMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Form_FormDefinitionHandler
 *
 * @author: wgl
 * @describe: 创建表单定义
 * @date: 2022/12/28 10:10
 */
@Component
@Slf4j
public class Form_FormDefinitionHandler implements CmdHandler<CreateFormDefinitionMessage> {

    @Autowired
    private HissFormModelService hissFormModelService;

    /**
     * 创建表单定义
     *
     * @param params
     * @param messageContext
     */
    @Override
    public void invoke(Message params, MessageContext messageContext) {
        try{
            hissFormModelService.saveFormDefinition((CreateFormDefinitionMessage) params,messageContext);
        }catch (Exception e){
            e.printStackTrace();
            messageContext.addError("msg","服务器处理错误："+e.getMessage());
        }
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.FORM_CREATE_FORM_DIFINITION.getId();
    }
}
