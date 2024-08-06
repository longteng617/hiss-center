package cn.itcast.hiss.form.annocation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * FieldName
 *
 * @author: wgl
 * @describe: 字段名
 * @date: 2022/12/28 10:10
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldName {

    /**
     * 字段名
     * @return
     */
    String value();
}
