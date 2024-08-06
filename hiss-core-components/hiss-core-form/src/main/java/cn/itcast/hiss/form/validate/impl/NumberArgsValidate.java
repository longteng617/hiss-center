package cn.itcast.hiss.form.validate.impl;

import cn.itcast.hiss.api.client.form.ArgsValidated;
import cn.itcast.hiss.form.validate.ArgValidate;
import cn.itcast.hiss.form.validate.FeedbackType;
import org.springframework.stereotype.Component;

/**
 * NumberArgsValidate
 *
 * @author: wgl
 * @describe: TODO
 * @date: 2022/12/28 10:10
 */
@Component
public class NumberArgsValidate implements ArgValidate {
    private static final String NUMBER_REGEX = "^[-+]?\\d+(\\.\\d+)?$";

    @Override
    public boolean validate(ArgsValidated validated) {
        String args = validated.getArgs();
        return args.matches(NUMBER_REGEX);
    }

    @Override
    public String getType() {
        return FeedbackType.NUMBER.name();
    }
}
