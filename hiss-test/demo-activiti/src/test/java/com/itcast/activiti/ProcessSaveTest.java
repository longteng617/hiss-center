package com.itcast.activiti;


import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Slf4j
public class ProcessSaveTest {

    @Autowired
    RepositoryService repositoryService;


    @ParameterizedTest
    @ValueSource(strings = {"/Users/dasouche/Downloads/bpmn-01.xml"})
    public void testSaveFromFile(String file){
        byte[] bytes = FileUtil.readBytes(file);
        Model model = repositoryService.newModel();
        model.setName("单人审批请假流程"); // 设置名称
        model.setCategory("学习"); // 设置分类
        repositoryService.saveModel(model);// 保存基本信息
        repositoryService.addModelEditorSource(model.getId(),bytes);// 保存xml信息
        assertNotNull(model.getId()); // 断言插入成功
        log.info("new model id : {}", model.getId());
    }
}
