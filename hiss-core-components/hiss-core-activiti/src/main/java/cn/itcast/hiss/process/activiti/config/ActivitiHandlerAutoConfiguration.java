package cn.itcast.hiss.process.activiti.config;


import cn.itcast.hiss.process.activiti.behavior.HissDefaultActivityBehaviorFactory;
import cn.itcast.hiss.process.activiti.mapper.*;
import cn.itcast.hiss.process.activiti.properties.MailServerInfoProperties;
import cn.itcast.hiss.process.activiti.properties.ScaffoldProperties;
import cn.itcast.hiss.process.activiti.service.UserTaskService;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

/**
 * ActivitiHandlerAutoConfiguration
 *
 * @author: wgl
 * @describe: 流程自动装配类
 * @date: 2022/12/28 10:10
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties({MailServerInfoProperties.class, ScaffoldProperties.class})
@ComponentScan("cn.itcast.hiss.process.activiti")
public class ActivitiHandlerAutoConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    public UserGroupManager userGroupManager() {
        return new UserGroupManager() {
            @Override
            public List<String> getUserGroups(String s) {
                return null;
            }

            @Override
            public List<String> getUserRoles(String s) {
                return null;
            }

            @Override
            public List<String> getGroups() {
                return null;
            }

            @Override
            public List<String> getUsers() {
                return null;
            }
        };
    }

    @Bean
    public SecurityManager securityManager() {
        return new SecurityManager() {
            @Override
            public String getAuthenticatedUserId() {
                return "miukoo-user-1";
            }

            @Override
            public List<String> getAuthenticatedUserGroups() throws SecurityException {
                return null;
            }

            @Override
            public List<String> getAuthenticatedUserRoles() throws SecurityException {
                return null;
            }
        };
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return mybatisPlusInterceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public HissDefaultActivityBehaviorFactory hissDefaultActivityBehaviorFactory() {
        return new HissDefaultActivityBehaviorFactory();
    }

    @Bean("hissUserTaskService")
    @ConditionalOnMissingBean
    public UserTaskService hissUserTaskService() {
        return new UserTaskService();
    }

    @Bean
    public MapperFactoryBean<HiActinstMapper> hiActinstMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<HiActinstMapper> factoryBean = new MapperFactoryBean<>(HiActinstMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    @Bean
    public MapperFactoryBean<HiCommentMapper> hiCommentMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<HiCommentMapper> factoryBean = new MapperFactoryBean<>(HiCommentMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }

    @Bean
    public MapperFactoryBean<HissAutoApprovalConfigMapper> hiAutoApprovalConfigMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<HissAutoApprovalConfigMapper> factoryBean = new MapperFactoryBean<>(HissAutoApprovalConfigMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }

    @Bean
    public MapperFactoryBean<GeBytearrayMapper> geBytearrayMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<GeBytearrayMapper> factoryBean = new MapperFactoryBean<>(GeBytearrayMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    @Bean
    public MapperFactoryBean<HissProcessPreLaunchMapper> hissProcessPreLaunchMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<HissProcessPreLaunchMapper> factoryBean = new MapperFactoryBean<>(HissProcessPreLaunchMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    @Bean
    public MapperFactoryBean<ActRuEventSubscrMapper> actRuEventSubscrMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<ActRuEventSubscrMapper> factoryBean = new MapperFactoryBean<>(ActRuEventSubscrMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    @Bean
    public MapperFactoryBean<HissProcessFormMapper> hissProcessFormMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<HissProcessFormMapper> factoryBean = new MapperFactoryBean<>(HissProcessFormMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    @Bean
    public MapperFactoryBean<HissUserAppMapper> hissUserAppMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<HissUserAppMapper> factoryBean = new MapperFactoryBean<>(HissUserAppMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    @Bean
    public MapperFactoryBean<HissSystemUserMapper> hissSystemUserMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<HissSystemUserMapper> factoryBean = new MapperFactoryBean<>(HissSystemUserMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    @Bean
    public MapperFactoryBean<HissProcessCategoryMapper> hissProcessCategoryMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<HissProcessCategoryMapper> factoryBean = new MapperFactoryBean<>(HissProcessCategoryMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    @Bean
    public MapperFactoryBean<ActReModelMapper> actReModelMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<ActReModelMapper> factoryBean = new MapperFactoryBean<>(ActReModelMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    @Bean
    public MapperFactoryBean<ProcessApplyMapper> processApplyMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<ProcessApplyMapper> factoryBean = new MapperFactoryBean<>(ProcessApplyMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    @Bean
    public MapperFactoryBean<ProcessHandleMapper> processHandleMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<ProcessHandleMapper> factoryBean = new MapperFactoryBean<>(ProcessHandleMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    @Bean
    public MapperFactoryBean<UpdateProcessTenantMapper> updateProcessTenantMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<UpdateProcessTenantMapper> factoryBean = new MapperFactoryBean<>(UpdateProcessTenantMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }

    @Bean
    public MapperFactoryBean<ProcessInstanceMapper> processInstanceMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<ProcessInstanceMapper> factoryBean = new MapperFactoryBean<>(ProcessInstanceMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }

    @Bean
    public MapperFactoryBean<HissProcessUpdateJobMapper> hissProcessUpdateJobMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<HissProcessUpdateJobMapper> factoryBean = new MapperFactoryBean<>(HissProcessUpdateJobMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
}
