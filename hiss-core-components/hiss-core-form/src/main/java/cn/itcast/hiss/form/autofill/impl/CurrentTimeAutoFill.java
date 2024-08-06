package cn.itcast.hiss.form.autofill.impl;

import cn.itcast.hiss.form.autofill.AutoFill;
import cn.itcast.hiss.form.autofill.AutoFillTypeEnum;
import cn.itcast.hiss.message.CurrentUser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * @author miukoo
 * @description 填充当前时间
 * @date 2023/6/27 9:10
 * @version 1.0
 **/
public class CurrentTimeAutoFill implements AutoFill {

    @Override
    public String fill(CurrentUser currentUser) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public AutoFillTypeEnum getType() {
        return AutoFillTypeEnum.CURRENT_TIME;
    }

}
