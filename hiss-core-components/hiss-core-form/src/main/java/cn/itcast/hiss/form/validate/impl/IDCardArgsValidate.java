package cn.itcast.hiss.form.validate.impl;

import cn.itcast.hiss.api.client.form.ArgsValidated;
import cn.itcast.hiss.form.validate.ArgValidate;
import cn.itcast.hiss.form.validate.FeedbackType;
import org.springframework.stereotype.Component;

import java.time.Year;

@Component
public class IDCardArgsValidate implements ArgValidate {

    @Override
    public boolean validate(ArgsValidated validated) {
        String args = validated.getArgs();
        // 校验出生年份
        String yearString = args.substring(6, 10);
        int year = Integer.parseInt(yearString);
        int currentYear = Year.now().getValue(); // 获取当前年份
        if (year < 1900 || year > currentYear) {
            return false;
        }

        // 校验出生月份
        String monthString = args.substring(10, 12);
        int month = Integer.parseInt(monthString);
        if (month < 1 || month > 12) {
            return false;
        }

        // 校验日期
        String dayString = args.substring(12, 14);
        int day = Integer.parseInt(dayString);
        if (day < 1 || day > 31) {
            return false;
        }

        return true;
    }

    @Override
    public String getType() {
        return FeedbackType.ID_CARD.name();
    }
}
