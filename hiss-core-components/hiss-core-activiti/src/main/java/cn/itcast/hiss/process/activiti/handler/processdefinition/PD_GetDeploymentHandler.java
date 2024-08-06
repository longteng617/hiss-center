package cn.itcast.hiss.process.activiti.handler.processdefinition;

import cn.hutool.core.bean.BeanUtil;
import cn.itcast.hiss.api.client.common.PageInfo;
import cn.itcast.hiss.cmd.handler.CmdHandler;
import cn.itcast.hiss.handler.HandlerIdClientEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.common.PageInfoMessage;
import cn.itcast.hiss.process.activiti.dto.processdefinition.DeploymentDTO;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * GetDeployments
 *
 * @author: wgl
 * @describe: 查询对应租户的流程部署
 * @date: 2022/12/28 10:10
 */
@Component
public class PD_GetDeploymentHandler implements CmdHandler<PageInfoMessage> {

    @Autowired
    RepositoryService repositoryService;


    /**
     * 获取流程部署
     *
     * @param params
     * @param messageContext
     */
    @Override
    public void invoke(Message params, MessageContext messageContext) {
        PageInfo payload = (PageInfo) params.getPalyload();
        List<DeploymentDTO> res = new ArrayList<>();
        List<Deployment> list = repositoryService.createDeploymentQuery()
                .deploymentTenantId(params.getMessageAuth().getTenant())
                .listPage(payload.getPageNum(), payload.getPageSize());
        for (Deployment item : list) {
            DeploymentDTO deploymentDTO = new DeploymentDTO();
            BeanUtil.copyProperties(item, deploymentDTO);
            String key = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(item.getId())
                    .singleResult()
                    .getKey();
            deploymentDTO.setKey(key);
            res.add(deploymentDTO);
        }
        messageContext.setResult(new ConcurrentHashMap<String, Object>() {
            {
                put("data", res);
                put("total", repositoryService.createDeploymentQuery().deploymentTenantId(params.getMessageAuth().getTenant()).count());
                put("msg", "查询成功");
            }
        });
    }


    @Override
    public String getId() {
        return HandlerIdClientEnum.PD_GET_DEPLOYMENT.getId();
    }
}
