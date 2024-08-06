package com.itcast.activiti;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Slf4j
public class ProcessRunTest {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;


    @Test
    public void testDeployment(){
        String modelId = "4b9a5a9c-4f1c-11ef-b27e-7e5b10447d5f";
        // 必须 bpmn20.xml 作为后缀名
        String xmlName = "bpmn-01.bpmn20.xml";
        // 查询model 基本信息
        Model model = repositoryService.getModel(modelId);
        // 读取 模型的 xml 数据
        byte[] xmlContent = repositoryService.getModelEditorSource(modelId);
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
                .category(model.getCategory())
                .name(model.getName())
                .addBytes(xmlName, xmlContent);
        // 部署 (把原模型文件复制一份)
        Deployment deploy = deploymentBuilder.deploy();
        model.setDeploymentId(deploy.getId());
        // 部署后的ID回写到模型中
        repositoryService.saveModel(model);

    }

    /**
     * 运行测试
     */
    @ParameterizedTest
    @ValueSource(strings = {"hiss_process_12_1722415883903"})
    public void testRun(String key){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key);
        assertNotNull(processInstance.getId()); // 断言插入成功
        log.info("new processInstance id : {}", processInstance.getId());
    }
}
