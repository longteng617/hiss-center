package cn.itcast.hiss.process.activiti.handler.history;

import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;

/**
 * GetInstancesByUserNameHandler
 *
 * @author: wgl
 * @describe: 获取用户历史handler
 * @date: 2022/12/28 10:10
 */
public class GetInstancesByUserNameHandler implements CmdHandler {

    @Override
    public void invoke(Message params, MessageContext messageContext) {

    }

    @Override
    public String getId() {
        return null;
    }
}
