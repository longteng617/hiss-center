package com.itheima.easy.process.listener;

import cn.itcast.hiss.client.handler.event.annotation.ActivitiTaskExpedite;
import cn.itcast.hiss.event.pojo.HissActivitiEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/*
 * @author miukoo
 * @description 监听催办消息
 * @date 2023/7/11 15:41
 * @version 1.0
 **/
@Component
@Slf4j
public class ExpediteListener {

    @ActivitiTaskExpedite
    public void expedite(HissActivitiEvent hissActivitiEvent){
        log.info("============收到催办任务了，业务系统可以再次推送消息给相关用户========:{}",hissActivitiEvent.getEventData());
    }

}
