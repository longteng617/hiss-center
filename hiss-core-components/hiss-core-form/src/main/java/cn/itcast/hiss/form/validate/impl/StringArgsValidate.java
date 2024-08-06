package cn.itcast.hiss.form.validate.impl;

import cn.itcast.hiss.api.client.form.ArgsValidated;
import cn.itcast.hiss.form.validate.ArgValidate;
import cn.itcast.hiss.form.validate.FeedbackType;
import org.springframework.stereotype.Component;

/**
 * StringArgsValidate
 *
 * @author: wgl
 * @describe: 字符串校验组件
 * @date: 2022/12/28 10:10
 */
@Component
public class StringArgsValidate implements ArgValidate {

    private static final String INPUT_REGEX = "^[\\p{L}\\p{N}\\p{P}\\p{Z}]+$";

    @Override
    public boolean validate(ArgsValidated validated) {
        String args = validated.getArgs();
        return args.matches(INPUT_REGEX);
    }

    @Override
    public String getType() {
        return FeedbackType.STRING.name();
    }
}
