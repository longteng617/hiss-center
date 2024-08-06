package cn.itcast.hiss.form.sql.manage;

import cn.itcast.hiss.form.sql.enums.FunctionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * SqlExchangeManage
 *
 * @author: wgl
 * @describe: sql转换管理器
 * @date: 2022/12/28 10:10
 */
@Slf4j
public class HissSqlExchangeManage implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    ApplicationContext applicationContext;

    private static Map<FunctionEnum, HissSqlExchangeHandler> sqlExchangeHandlerMap = new HashMap<>();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("系统sql转换器初始化开始");
        Map<String, HissSqlExchangeHandler> beansOfType = applicationContext.getBeansOfType(HissSqlExchangeHandler.class);
        beansOfType.values().forEach(e -> {
            log.info("{}sql转换器开始执行初始化", e.getFunctionEnum());
            sqlExchangeHandlerMap.put(e.getFunctionEnum(), e);
            log.info("{}sql转换器初始化完成", e.getFunctionEnum());
        });
    }

    public static HissSqlExchangeHandler getSqlExchangeHandler(FunctionEnum functionEnum){
        return sqlExchangeHandlerMap.get(functionEnum);
    }
}