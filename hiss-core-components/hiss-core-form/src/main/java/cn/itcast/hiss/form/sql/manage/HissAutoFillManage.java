package cn.itcast.hiss.form.sql.manage;

import cn.itcast.hiss.form.autofill.AutoFill;
import cn.itcast.hiss.form.autofill.AutoFillManager;
import cn.itcast.hiss.form.validate.ArgValidate;
import cn.itcast.hiss.form.validate.ValidateManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;

/**
 * HissAutoFillManage
 *
 * @author: miukoo
 * @describe: 自动填充字段
 * @date: 2022/12/28 10:10
 */
@Slf4j
public class HissAutoFillManage implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("系统参数自动填充初始化开始！！！");
        scanAutoFill();
        log.info("系统参数自动填充初始化完成！！！");
    }


    /**
     * 扫描所有的过滤器
     */
    private void scanAutoFill() {
        Map<String, AutoFill> beans = applicationContext.getBeansOfType(AutoFill.class);
        beans.values().stream().forEach(v -> {
            AutoFillManager.addAutoFill(v);
        });
    }
}
