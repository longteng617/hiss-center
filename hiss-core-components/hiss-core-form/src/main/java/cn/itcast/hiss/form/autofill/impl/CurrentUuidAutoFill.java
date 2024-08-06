package cn.itcast.hiss.form.autofill.impl;

import cn.itcast.hiss.form.autofill.AutoFill;
import cn.itcast.hiss.form.autofill.AutoFillTypeEnum;
import cn.itcast.hiss.message.CurrentUser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/*
 * @author miukoo
 * @description 填充UUID
 * @date 2023/6/27 9:10
 * @version 1.0
 **/
public class CurrentUuidAutoFill implements AutoFill {

    @Override
    public String fill(CurrentUser currentUser) {
        return UUID.randomUUID().toString().replace("-","");
    }

    @Override
    public AutoFillTypeEnum getType() {
        return AutoFillTypeEnum.CURRENT_UUID;
    }

}
