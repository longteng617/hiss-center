package cn.itcast.hiss.form.autofill;

import cn.itcast.hiss.form.validate.ArgValidate;
import cn.itcast.hiss.message.CurrentUser;

import java.util.HashMap;
import java.util.Map;

/**
 * AutoFillManager
 *
 * @author: miukoo
 * @describe: 自动填充管理器
 * @date: 2022/12/28 10:10
 */
public class AutoFillManager {

    private static Map<String, AutoFill> AUTO_FILL_MAP = new HashMap<>();

    public static void addAutoFill(AutoFill autoFill) {
        AUTO_FILL_MAP.put(autoFill.getType().name(), autoFill);
    }

    public static String autoFill(String type, CurrentUser currentUser) {
        AutoFill autoFill = AUTO_FILL_MAP.get(type.toUpperCase());
        if (autoFill == null) {
            return null;
        }
        return autoFill.fill(currentUser);
    }

    public static boolean canAutoFill(String type) {
        AutoFill autoFill = AUTO_FILL_MAP.get(type.toUpperCase());
        if (autoFill == null) {
            return false;
        }
        return true;
    }
}
