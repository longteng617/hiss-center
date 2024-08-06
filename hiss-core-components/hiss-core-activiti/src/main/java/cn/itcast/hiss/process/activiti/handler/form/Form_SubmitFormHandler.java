package cn.itcast.hiss.process.activiti.handler.form;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.form.FormSubmitData;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.form.service.HissFormModelService;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.form.CreateFormDefinitionMessage;
import cn.itcast.hiss.message.sender.form.FormSubmitDataMessage;
import cn.itcast.hiss.process.activiti.mapper.HissProcessFormMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Form_SubmitFormHandler
 *
 * @author: wgl
 * @describe: 提交数据
 * @date: 2022/12/28 10:10
 */
@Component
public class Form_SubmitFormHandler implements CmdHandler<FormSubmitDataMessage> {

    @Autowired
    private HissFormModelService hissFormModelService;
    @Autowired
    private HissProcessFormMapper hissProcessFormMapper;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        try{
            FormSubmitData palyload = (FormSubmitData) params.getPalyload();
            hissFormModelService.submitFormData((FormSubmitDataMessage) params, messageContext);
            // 如果数据保存成功，则把数据同步保存到配置表
            if(messageContext.isSuccess()&& StrUtil.isNotEmpty(palyload.getModelId())){
                String dataId = (String) messageContext.getResult().get("result");
                hissProcessFormMapper.updateDataIdBy(palyload.getFormId(),dataId,palyload.getModelId());
            }
        }catch (Exception e){
            e.printStackTrace();
            messageContext.addError("msg","服务器处理错误："+e.getMessage());
        }
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.FORM_SUBMIT_DATA.getId();
    }
}
