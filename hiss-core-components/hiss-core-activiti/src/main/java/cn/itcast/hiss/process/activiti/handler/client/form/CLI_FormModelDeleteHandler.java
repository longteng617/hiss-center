package cn.itcast.hiss.process.activiti.handler.client.form;

import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.form.mapper.HissFormModelMapper;
import cn.itcast.hiss.form.pojo.HissFormModel;
import cn.itcast.hiss.form.service.HissFormModelService;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sys.ProcessModelMessage;
import cn.itcast.hiss.message.sys.pojo.ProcessModel;
import cn.itcast.hiss.process.activiti.mapper.HissProcessFormMapper;
import cn.itcast.hiss.process.activiti.mapper.HissUserAppMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 *
 * @author: miukoo
 * @describe: 用户表单删除
 * @date: 2022/12/28 10:10
 */
@Component
@Slf4j
public class CLI_FormModelDeleteHandler implements CmdHandler<ProcessModelMessage> {

    @Autowired
    private HissUserAppMapper hissUserAppMapper;
    @Autowired
    private HissFormModelMapper hissFormModelMapper;
    @Autowired
    private HissProcessFormMapper hissProcessFormMapper;
    @Autowired
    private HissFormModelService hissFormModelService;

    @Override
    public void invoke(Message params, MessageContext messageContext) {
        ProcessModel processModel = (ProcessModel) params.getPalyload();
        HissFormModel hissFormModel = hissFormModelMapper.selectById(processModel.getId());
        if(hissFormModel!=null){
            boolean canEdit = isAdmin(params);
            if(!canEdit){
                List<String> userAppIdsList = hissUserAppMapper.getUserAppIdsList(processModel.getAdminId());
                canEdit = userAppIdsList.contains(hissFormModel.getTenantId());
            }
            if(canEdit){
                long count = hissProcessFormMapper.countFormId(hissFormModel.getId(),hissFormModel.getTenantId());
                if(count==0){
                    hissFormModelService.deleteByModelId(hissFormModel.getId());
                    messageContext.addResultAndCount("msg","操作成功");
                }else{
                    messageContext.addError("msg","存在相关流程实例，不允许删除");
                }
            }else{
                messageContext.addError("msg","无权限操作");
            }
        }else{
            messageContext.addError("msg","删除的数据不存在或已经被删除");
        }
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.CLI_FORM_MODEL_DELETE.getId();
    }
}
