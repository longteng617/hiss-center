package cn.itcast.hiss.process.activiti.handler.form;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.dto.FormServerDefinitionDTO;
import cn.itcast.hiss.api.client.form.GetFormDefinition;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.common.SystemConstant;
import cn.itcast.hiss.execption.auth.ParamExecption;
import cn.itcast.hiss.form.service.HissFormModelService;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.form.GetFormDefinitionByIdMessage;
import cn.itcast.hiss.server.template.HissServerApperanceTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Form_AcitivitiDefinitionGetFormHandler
 *
 * @author: wgl
 * @describe: 流程定义阶段获取表单明细数据
 * @date: 2022/12/28 10:10
 */
@Component
@Slf4j
public class Form_AcitivitiDefinitionGetFormHandler implements CmdHandler<GetFormDefinitionByIdMessage> {


    @Autowired
    private HissFormModelService hissFormModelService;

    @Autowired
    private HissServerApperanceTemplate hissServerApperanceTemplate;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        GetFormDefinition payload = (GetFormDefinition) params.getPalyload();
        log.info("流程定义阶段获取表单:{}", payload);
        String type = payload.getType();
        if (StrUtil.isEmpty(type)) {
            //默认是内部表单
            doInnerFormInvoke(payload, messageContext, params.getMessageAuth().getTenant());
        } else if (SystemConstant.FORM_INNER_FORM.equals(type)) {
            //内部表单
            doInnerFormInvoke(payload, messageContext, params.getMessageAuth().getTenant());
        } else if (SystemConstant.FORM_OUTER_FORM.equals(type)) {
           // TODO 预留接口 : 一期不实现
        } else {
            throw new ParamExecption("表单类型参数错误,type需要指明是内部表单还是外部表单");
        }
    }

    /**
     * 执行外部表单的获取
     *
     * @param getFormDefinition
     * @param messageContext
     */
    private void doOutterFormInvoke(GetFormDefinition getFormDefinition, MessageContext messageContext) {
        FormServerDefinitionDTO formDefinition = hissServerApperanceTemplate.getFormDefinition(getFormDefinition);
        messageContext.addResultAndCount("result", formDefinition);
    }

    /**
     * 执行内部表单的获取
     *
     * @param getFormDefinition
     * @param messageContext
     * @param tenant
     */
    private void doInnerFormInvoke(GetFormDefinition getFormDefinition, MessageContext messageContext, String tenant) {
        List<FormServerDefinitionDTO> formDefinition = hissFormModelService.queryFormInfo(getFormDefinition,messageContext);
        messageContext.addResultAndCount("result", formDefinition);
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.FORM_ACTIVITI_DEFINITION_GET_FORM_DIFINITION.getId();
    }
}
