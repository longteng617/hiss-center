package cn.itcast.hiss.form.validate.impl;

import cn.itcast.hiss.api.client.form.ArgsValidated;
import cn.itcast.hiss.form.validate.ArgValidate;
import cn.itcast.hiss.form.validate.FeedbackType;
import cn.itcast.hiss.form.validate.ValidateManager;
import org.springframework.stereotype.Component;

/**
 * NoneArgValidate
 *
 * @author: wgl
 * @describe: 不校验
 * @date: 2022/12/28 10:10
 */
@Component
public class NoneArgValidate implements ArgValidate{

    @Override
    public boolean validate(ArgsValidated validated) {
        String args = validated.getArgs();
        return true;
    }

    @Override
    public String getType() {
        return FeedbackType.NONE.name();
    }
}
