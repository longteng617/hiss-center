package cn.itcast.hiss.process.activiti.handler.form;

import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.form.validate.FeedbackType;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Form_ValidateTypeHandler
 *
 * @author: wgl
 * @describe: 获取系统所有校验类型
 * @date: 2022/12/28 10:10
 */
@Component
public class Form_ValidateTypeHandler implements CmdHandler {
    @Override
    public void invoke(Message params, MessageContext messageContext) {
        ArrayList<Map> res = new ArrayList<>();
        FeedbackType[] values = FeedbackType.values();
        for (FeedbackType index: values) {
            HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
            objectObjectHashMap.put("name",index.name());
            objectObjectHashMap.put("type",index.getTypeName());
        }
        messageContext.addResultAndCount("result",res);
    }

    @Override
    public String getId() {
        return HandlerIdClientEnum.FORM_TASK_VALIDATE_TYPE.getId();
    }
}
