package cn.itcast.hiss.form.autofill;

import lombok.AllArgsConstructor;

/*
 * @author miukoo
 * @description 自动填充类型
 * @date 2023/6/27 9:03
 * @version 1.0
 **/
@AllArgsConstructor
public enum AutoFillTypeEnum {

    CURRENT_TIME("当前时间"),
    CURRENT_UUID("随机UUID"),
    CURRENT_USER_ID("当前登录用户ID"),
    CURRENT_USER_NAME("当前登录用户名"),
    CURRENT_USER_DEPT_ID("当前登录用户部门ID"),
    CURRENT_USER_DEPT_NAME("当前登录用户部门名")
    ;
    String note;
}
