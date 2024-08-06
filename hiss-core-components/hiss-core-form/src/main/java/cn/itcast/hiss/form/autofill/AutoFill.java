package cn.itcast.hiss.form.autofill;

import cn.itcast.hiss.message.CurrentUser;

/*
 * @author miukoo
 * @description 自动填充支持的超级类
 * @date 2023/6/27 9:08
 * @version 1.0
 **/
public interface AutoFill {

    public String fill(CurrentUser currentUser);

    public AutoFillTypeEnum getType();
}
