package cn.itcast.hiss.form.validate.impl;

import cn.itcast.hiss.api.client.form.ArgsValidated;
import cn.itcast.hiss.form.validate.ArgValidate;
import cn.itcast.hiss.form.validate.FeedbackType;
import org.springframework.stereotype.Component;

/**
 * UrlArgsValidate
 *
 * @author: wgl
 * @describe: URL校验
 * @date: 2022/12/28 10:10
 */
@Component
public class UrlArgsValidate  implements ArgValidate {

    @Override
    public boolean validate(ArgsValidated validated) {
        String args = validated.getArgs();
        return args.contains("www.");
    }

    @Override
    public String getType() {
        return FeedbackType.URL.name();
    }
}
