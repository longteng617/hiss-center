package cn.itcast.hiss.form.sql.manage;

import cn.itcast.hiss.form.validate.ArgValidate;
import cn.itcast.hiss.form.validate.ValidateManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;

/**
 * HissArgValidateManage
 *
 * @author: wgl
 * @describe: 参数校验器管理器
 * @date: 2022/12/28 10:10
 */
@Slf4j
public class HissArgValidateManage implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("系统参数校验器初始化开始！！！");
        scanValidate();
        log.info("系统参数校验器初始化完成！！！");
    }


    /**
     * 扫描所有的过滤器
     */
    private void scanValidate() {
        Map<String, ArgValidate> beans = applicationContext.getBeansOfType(ArgValidate.class);
        beans.values().stream().forEach(v -> {
            ValidateManager.addValidate(v);
        });
    }
}