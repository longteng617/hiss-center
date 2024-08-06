package cn.itcast.hiss.common.enums;

/*
 * @author miukoo
 * @description 支持的表单类型
 * @date 2023/6/6 13:03
 * @version 1.0
 **/
public enum FormConfigTypeEnum {

    EXTERNALFORM,INNERFORM;

    public static FormConfigTypeEnum stringToEnum(String name) {
        for (FormConfigTypeEnum type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
