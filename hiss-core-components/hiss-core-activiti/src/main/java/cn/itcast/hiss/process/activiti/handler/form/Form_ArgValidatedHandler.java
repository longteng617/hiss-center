package cn.itcast.hiss.process.activiti.handler.form;

import cn.itcast.hiss.api.client.form.ArgsValidated;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.form.service.HissFormModelService;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.form.ArgsValidateMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Form_ArgValidatedHandler
 *
 * @author: wgl
 * @describe: 表单参数校验处理器
 * @date: 2022/12/28 10:10
 */
@Component
@Slf4j
public class Form_ArgValidatedHandler implements CmdHandler<ArgsValidateMessage> {


    @Autowired
    private HissFormModelService hissFormModelService;
    /**
     * 参数校验
     * @param params
     * @param messageContext
     */
    @Override
    public void invoke(Message params, MessageContext messageContext) {
        hissFormModelService.argsValidated((ArgsValidateMessage)params,messageContext);
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.FORM_ARG_VALIDATED.getId();
    }
}
