package cn.itcast.hiss.form.service;

import cn.itcast.hiss.api.client.dto.FormServerDefinitionDTO;
import cn.itcast.hiss.api.client.form.*;
import cn.itcast.hiss.form.pojo.HissFormTableFields;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.form.ArgsValidateMessage;
import cn.itcast.hiss.message.sender.form.CreateFormDefinitionMessage;
import cn.itcast.hiss.message.sender.form.FormDataListMessage;
import cn.itcast.hiss.message.sender.form.FormSubmitDataMessage;

import java.util.List;

/**
 *
 * @author: miuko
 * @describe: 设计表单的保存与更新
 * @date: 2022/12/28 10:10
 */
public interface HissFormModelService {

    /**
     * 保存或更新表单定义
     *
     */
    public void saveFormDefinition(CreateFormDefinitionMessage message, MessageContext messageContext);

    void queryForm(GetFormDefinition formDefinition, MessageContext messageContext);

    /**
     * 流程配置时获取的表单内容
     */
     List<FormServerDefinitionDTO> queryFormInfo(GetFormDefinition formDefinition, MessageContext messageContext) ;

    void submitFormData(FormSubmitDataMessage params, MessageContext messageContext) throws Exception;

    HissFormTableFields findHissFormTableFields(List<HissFormTableFields> hissFormTables, String key );


    void argsValidated(ArgsValidateMessage params, MessageContext messageContext);

    void queryFormData(FormSubmitDataMessage params, MessageContext messageContext);

    void deleteByModelId(String modelId);

    void queryFormField(GetFormDefinition formDefinition, MessageContext messageContext);

    void copyFormData(String formId, String oldDataId, String newDataId);

    void queryAutoFillData(FormSubmitDataMessage params, MessageContext messageContext);
}
