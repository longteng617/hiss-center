package cn.itcast.hiss.form.validate.impl;

import cn.itcast.hiss.api.client.form.ArgsValidated;
import cn.itcast.hiss.form.validate.ArgValidate;
import cn.itcast.hiss.form.validate.FeedbackType;
import org.springframework.stereotype.Component;

/**
 * IntegerArgsValidate
 *
 * @author: wgl
 * @describe: 整数校验
 * @date: 2022/12/28 10:10
 */
@Component
public class IntegerArgsValidate implements ArgValidate {
    private static final String INTEGER_REGEX = "^[-+]?\\d+$";

    @Override
    public boolean validate(ArgsValidated validated) {
        String args = validated.getArgs();
        return args.matches(INTEGER_REGEX);
    }

    @Override
    public String getType() {
        return FeedbackType.INTEGER.name();
    }
}

