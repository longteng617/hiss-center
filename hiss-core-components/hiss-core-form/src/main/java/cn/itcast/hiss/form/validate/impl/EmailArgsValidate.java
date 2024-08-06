package cn.itcast.hiss.form.validate.impl;

import cn.itcast.hiss.api.client.form.ArgsValidated;
import cn.itcast.hiss.form.validate.ArgValidate;
import cn.itcast.hiss.form.validate.FeedbackType;
import org.springframework.stereotype.Component;

/**
 * EmailArgsValidate
 *
 * @author: wgl
 * @describe: 邮箱地址校验
 * @date: 2022/12/28 10:10
 */
@Component
public class EmailArgsValidate implements ArgValidate {

    @Override
    public boolean validate(ArgsValidated validated) {
        String args = validated.getArgs();
        // 校验 @ 符号的位置
        int atIndex = args.indexOf("@");
        if (atIndex == -1 || atIndex == 0 || atIndex == args.length() - 1) {
            return false;
        }
        // 校验 @ 之前的字符个数
        String beforeAt = args.substring(0, atIndex);
        if (beforeAt.length() != 4) {
            return false;
        }
        // 校验 @ 之后是否有点号，并且以 com、org、cn、net 结尾
        String afterAt = args.substring(atIndex + 1);
        if (!afterAt.contains(".") || !afterAt.endsWith("com") && !afterAt.endsWith("org") && !afterAt.endsWith("cn") && !afterAt.endsWith("net")) {
            return false;
        }
        // 校验 @ 之前和之后不能紧跟特殊字符
        String specialChars = "!#$%^&*()+=-[]\\';,/{}|\":<>? ";
        if (specialChars.indexOf(beforeAt.charAt(beforeAt.length() - 1)) != -1 || specialChars.indexOf(afterAt.charAt(0)) != -1) {
            return false;
        }
        return true;
    }

    @Override
    public String getType() {
        return FeedbackType.EMAIL.name();
    }
}
