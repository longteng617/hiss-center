package cn.itcast.hiss.form.validate;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 反馈类型
 */
@AllArgsConstructor
@Getter
public enum FeedbackType{
    /**
     * 无 不限制任何输入
     */
    NONE("NONE","不限制任何输入"),
    /**
     * 字符串 可输入、中文、英文大小写、数字、特殊符号
     */
    STRING("STRING","字符串"),
    /**
     * 数字
     * 1、只可输入数字字符
     * 2、可以有正负号
     * 3、可以有一个小数点
     * 4、小数点前后有数字
     */
    NUMBER("NUMBER","数字"),
    /**
     * 整数
     * 1、只可输入数字字符
     * 2、无特殊符号、小数点等
     * 3、可以有正负号
     */
    INTEGER("INTEGER","整数"),
    /**
     * 小数
     * 1、数字字符
     * 2、必须有、且只能有一个小数点
     * 3、可以有正负号
     * 4、小数点前后有数字
     */
    DECIMAL("DECIMAL","小数"),
    /**
     * URL地址
     * 1、校验是否包含www.前缀
     */
    URL("URL","URL地址"),
    /**
     * 邮箱地址
     * 1、有且只有一个@
     * 2、@不能放在开头，也不能放在结尾
     * 3、@之后必须有
     * 4、@之前或之后不能紧跟
     * 5、@之前要有4个字符
     * 6、以com、org、cn、net结尾
     */
    EMAIL("EMAIL","邮箱地址"),
    /**
     * 手机号码
     * 1、手机号码段:前缀要求13、14、15、16、17、18、19开头。
     * 2、位数要求:长度为11位。
     */
    PHONE("PHONE","手机号码"),
    /**
     * 身份证号码
     * 1、取第7到10位的字符，校验出生年份: 是否在1900年到2022年之间的;
     * 2、取第11到12位的字符，校验出生月份: 是否在1-12;
     * 3、取第13到14位校验日期: 是否在1-31;
     */
    ID_CARD("ID_CARD", "身份证号码"),

    /**
     * 唯一值校验
     */
    ONLY_ONE_VALUE("ONLY_ONE_VALUE", "唯一值校验"),
    ;

    private String name;

    private String typeName;

}