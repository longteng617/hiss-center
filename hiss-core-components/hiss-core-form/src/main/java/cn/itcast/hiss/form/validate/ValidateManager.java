package cn.itcast.hiss.form.validate;

import cn.itcast.hiss.api.client.form.ArgsValidated;

import java.util.HashMap;
import java.util.Map;

/**
 * ValidateManager
 *
 * @author: wgl
 * @describe: 参数校验管理器
 * @date: 2022/12/28 10:10
 */
public class ValidateManager {

    private static Map<String, ArgValidate> validateMap = new HashMap<String, ArgValidate>();

    /**
     * 服务启动的时候
     *
     * @param argValidate
     */
    public static void addValidate(ArgValidate argValidate) {
        validateMap.put(argValidate.getType(), argValidate);
    }

    public static boolean validate(String type, ArgsValidated args) {
        ArgValidate argValidate = validateMap.get(type.toUpperCase());
        if (argValidate == null) {
            return false;
        }
        return argValidate.validate(args);
    }

    public static boolean canValidate(String type) {
        ArgValidate argValidate = validateMap.get(type.toUpperCase());
        if (argValidate == null) {
            return false;
        }
        return true;
    }
}
