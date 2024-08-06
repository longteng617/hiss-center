package cn.itcast.hiss.process.activiti.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.processdefinition.ProcessDesignModel;
import cn.itcast.hiss.common.enums.ModelTypeEnum;
import cn.itcast.hiss.message.Message;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.processdefinition.ProcessDesignModelMessage;
import cn.itcast.hiss.process.activiti.mapper.ActReModelMapper;
import cn.itcast.hiss.process.activiti.mapper.GeBytearrayMapper;
import cn.itcast.hiss.process.activiti.pojo.ActReModel;
import cn.itcast.hiss.process.activiti.service.ActivitiDesignerService;
import cn.itcast.hiss.process.activiti.util.AdminUtil;
import cn.itcast.hiss.process.activiti.util.ParseBusinessUtil;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 业务模式的实现
 * @author miukoo
 * @description
 * @date 2023/6/6 10:28
 * @version 1.0
 **/
@Service
@Transactional
public class ActivitiDesignerServiceImpl implements ActivitiDesignerService {

    @Autowired
    RepositoryService repositoryService;
    @Autowired
    GeBytearrayMapper geBytearrayMapper;
    @Autowired
    ActReModelMapper actReModelMapper;

    @Override
    public void saveBis(ProcessDesignModelMessage message, MessageContext messageContext) {
        ProcessDesignModel processDesignModel = (ProcessDesignModel) message.getPalyload();
        String xml = processDesignModel.getContent();
        String configJson = processDesignModel.getConfigJson();
        try {
            String xmlNew = ParseBusinessUtil.getXml(configJson, xml);
            String tenantId = message.getMessageAuth().getTenant();
            Model model = null;
            if (StrUtil.isNotEmpty(processDesignModel.getModelId())) {
                model = repositoryService.getModel(processDesignModel.getModelId());
            }
            if (model == null) {
                model = repositoryService.newModel();
                model.setKey(ModelTypeEnum.BIS.name());
            }else{ // 权限验证
                boolean canEdit = AdminUtil.isAdmin(message);
                if(!canEdit){
                    canEdit = message.getMessageAuth().getTenant().equalsIgnoreCase(tenantId);
                }
                if(!canEdit){
                    messageContext.addError("msg","无权限操作");
                    return;
                }
            }
            if (model != null) {
                if (StrUtil.isNotEmpty(processDesignModel.getName())) {
                    model.setName(processDesignModel.getName());
                }
                if (StrUtil.isNotEmpty(processDesignModel.getCategory())) {
                    model.setCategory(processDesignModel.getCategory());
                }
                model.setTenantId(tenantId);
                repositoryService.saveModel(model);// 保存基本信息
                if(StrUtil.isNotEmpty(processDesignModel.getIcon())){// 更新图标
                    actReModelMapper.updateIconById(processDesignModel.getIcon(), model.getId(),processDesignModel.getDescription());
                }
                repositoryService.addModelEditorSource(model.getId(),xml.getBytes());// 保存源码信息
                repositoryService.addModelEditorSourceExtra(model.getId(),xmlNew.getBytes());// 运行XML
                geBytearrayMapper.deleteModelConfigJson(model.getId());
                geBytearrayMapper.insertModelConfigJson(UUID.randomUUID().toString(),configJson, model.getId());
                messageContext.addResultAndCount("modelId", model.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageContext.addError("msg", e.getMessage());
        }
    }


    /**
     * 验证设计是否合法
     * @param message
     * @param messageContext
     */
    @Override
    public void verification(ProcessDesignModelMessage message, MessageContext messageContext) {
        ProcessDesignModel processDesignModel = (ProcessDesignModel) message.getPalyload();
        String xml = processDesignModel.getContent();
        String configJson = processDesignModel.getConfigJson();
        try {
            ParseBusinessUtil.getXml(configJson, xml);
            messageContext.addResultAndCount("msg","验证成功");
        } catch (Exception e) {
            e.printStackTrace();
            messageContext.addError("msg",e.getMessage());
        }
    }

    /**
     * 业务模式转为开发模式
     * @param message
     * @param messageContext
     */
    @Override
    public void convertToDev(ProcessDesignModelMessage message, MessageContext messageContext) {
        ProcessDesignModel processDesignModel = (ProcessDesignModel) message.getPalyload();
        Model model = null;
        if (StrUtil.isNotEmpty(processDesignModel.getModelId())) {
            model = repositoryService.getModel(processDesignModel.getModelId());
        }
        if (model == null) {
            messageContext.addError("msg","没有对应的模型");
        }else{
           if(ModelTypeEnum.BIS.name().equals(model.getKey())){
               String busId = actReModelMapper.getModelBusId(model.getId());
               Model newModel = null;
               if(StrUtil.isNotEmpty(busId)){
                   newModel = repositoryService.getModel(busId);
               }
               if(newModel==null){
                   newModel = repositoryService.newModel();
               }
               newModel.setKey(ModelTypeEnum.DEV.name());
               newModel.setCategory(model.getCategory());
               newModel.setName(model.getName());
               newModel.setTenantId(model.getTenantId());
               repositoryService.saveModel(newModel);// 保存基本信息
               actReModelMapper.updateBisById(model.getId(),newModel.getId());
               repositoryService.addModelEditorSource(newModel.getId(), repositoryService.getModelEditorSourceExtra(model.getId()));// 保存源码信息
               actReModelMapper.updateBisById(newModel.getId(),model.getId());
               repositoryService.saveModel(model);
               messageContext.addResult("modelId", newModel.getId());
           }else{
               messageContext.addError("msg","非业务模式不能转为开发模式");
           }
        }
    }

    public void saveDev(Message message, MessageContext messageContext) {
        ProcessDesignModel processDesignModel = (ProcessDesignModel) message.getPalyload();
        Model model = null;
        if (StrUtil.isNotEmpty(processDesignModel.getModelId())) {
            model = repositoryService.getModel(processDesignModel.getModelId());
        }
        if (model == null) {
            model = repositoryService.newModel();
            model.setKey(ModelTypeEnum.DEV.name());
        }
        if (model != null) {
            if (StrUtil.isNotEmpty(processDesignModel.getName())) {
                model.setName(processDesignModel.getName());
            }
            if (StrUtil.isNotEmpty(processDesignModel.getBusinessKey())) {
                model.setKey(processDesignModel.getBusinessKey());
            }
            if (StrUtil.isNotEmpty(processDesignModel.getCategory())) {
                model.setCategory(processDesignModel.getCategory());
            }
            model.setTenantId(message.getMessageAuth().getTenant());
            repositoryService.saveModel(model);// 保存基本信息
            repositoryService.addModelEditorSource(model.getId(), processDesignModel.getContent().getBytes());// 保存源码信息
            String configJson = processDesignModel.getConfigJson();
            if(StrUtil.isNotEmpty(configJson)) {
                geBytearrayMapper.deleteModelConfigJson(model.getId());
                geBytearrayMapper.insertModelConfigJson(UUID.randomUUID().toString(), configJson, model.getId());
            }
            messageContext.addResult("modelId", model.getId());
        }
    }

    /**
     * 获取内容
     * @param message
     * @param messageContext
     */
    public void get(Message message, MessageContext messageContext) {
        ProcessDesignModel processDesignModel = (ProcessDesignModel) message.getPalyload();
        ActReModel model = null;
        if (StrUtil.isNotEmpty(processDesignModel.getModelId())) {
            model = actReModelMapper.selectById(processDesignModel.getModelId());
            if (model != null) {
                processDesignModel.setConfigJson(geBytearrayMapper.getModelConfigJson(model.getId()));
                processDesignModel.setIcon(model.getIcon());
                processDesignModel.setDescription(model.getDescription());
                processDesignModel.setCategory(model.getCategory());
                processDesignModel.setType(ModelTypeEnum.valueOf(model.getKey()));
                processDesignModel.setName(model.getName());
                processDesignModel.setContent(new String(repositoryService.getModelEditorSource(model.getId())));
            }
        }
        messageContext.addResult("model", processDesignModel);
    }

}
