package cn.itcast.hiss.form.validate;

import cn.itcast.hiss.api.client.form.ArgsValidated;

/**
 * 参数校验模板类
 */
public interface ArgValidate {

    public boolean validate(ArgsValidated args);


    public String getType();

}
