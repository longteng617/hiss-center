package cn.itcast.hiss.form.validate.impl;

import cn.itcast.hiss.api.client.form.ArgsValidated;
import cn.itcast.hiss.form.validate.ArgValidate;
import cn.itcast.hiss.form.validate.FeedbackType;
import org.springframework.stereotype.Component;

/**
 * PhoneArgsValidate
 *
 * @author: wgl
 * @describe: TODO
 * @date: 2022/12/28 10:10
 */
@Component
public class PhoneArgsValidate implements ArgValidate {

    @Override
    public boolean validate(ArgsValidated validated) {
        String args = validated.getArgs();
        // 校验长度
        if (args.length() != 11) {
            return false;
        }

        // 校验前缀
        String prefix = args.substring(0, 2);
        if (!prefix.equals("13") && !prefix.equals("14") && !prefix.equals("15") && !prefix.equals("16") &&
                !prefix.equals("17") && !prefix.equals("18") && !prefix.equals("19")) {
            return false;
        }

        return true;
    }

    @Override
    public String getType() {
        return FeedbackType.PHONE.name();
    }
}
