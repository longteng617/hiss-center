package cn.itcast.hiss.process.activiti.servicetask;

import cn.hutool.extra.spring.SpringUtil;
import cn.itcast.hiss.common.enums.HissTaskTypeEnum;
import cn.itcast.hiss.process.activiti.service.ActivitiService;
import lombok.Data;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.FixedValue;

/*
 * @author miukoo
 * @description 知会节点自动处理
 * @date 2023/6/9 10:01
 * @version 1.0
 **/
@Data
public class NotifierServiceTask implements JavaDelegate {

    // 表达式，需要执行
    Expression expression;
    // 固定人员列表
    FixedValue users;
    private static ActivitiService activitiService = SpringUtil.getBean(ActivitiService.class);
    @Override
    public void execute(DelegateExecution execution) {
        String temp = null;
        if(users!=null){
            temp = users.getExpressionText();
        }
        activitiService.doCCorNotifierTask(execution,temp,expression, HissTaskTypeEnum.NOTIFICATION);

    }

}
