package cn.itcast.hiss.form.config;

import cn.itcast.hiss.form.mapper.*;
import cn.itcast.hiss.form.sql.manage.HissArgValidateManage;
import cn.itcast.hiss.form.sql.manage.HissSqlExchangeManage;
import cn.itcast.hiss.form.validate.ValidateManager;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * HissFormAutoConfiguration
 *
 * @author: wgl
 * @describe: hiss表单自动配置类
 * @date: 2022/12/28 10:10
 */
@Configuration
@ComponentScan({"cn.itcast.hiss.form"})
public class HissFormAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HissSqlExchangeManage sqlExchangeManageInit() {
        HissSqlExchangeManage hissSqlExchangeManage = new HissSqlExchangeManage();
        return hissSqlExchangeManage;
    }

    @Bean
    @ConditionalOnMissingBean
    public HissArgValidateManage argValidateManageInit() {
        HissArgValidateManage hissArgValidateManage = new HissArgValidateManage();
        return hissArgValidateManage;
    }

    @Bean
    @ConditionalOnMissingBean
    public ValidateManager validateManagerInit() {
        ValidateManager validateManager = new ValidateManager();
        return validateManager;
    }

    @Bean
    public MapperFactoryBean<HissFormBytearrayMapper> hissFormBytearrayMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<HissFormBytearrayMapper> factoryBean = new MapperFactoryBean<>(HissFormBytearrayMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }

    @Bean
    public MapperFactoryBean<HissFormModelMapper> hissFormModelMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<HissFormModelMapper> factoryBean = new MapperFactoryBean<>(HissFormModelMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }

    @Bean
    public MapperFactoryBean<HissFormTableFieldsMapper> hissFormTableFieldsMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<HissFormTableFieldsMapper> factoryBean = new MapperFactoryBean<>(HissFormTableFieldsMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }

    @Bean
    public MapperFactoryBean<HissFormTablesMapper> hissFormTablesMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<HissFormTablesMapper> factoryBean = new MapperFactoryBean<>(HissFormTablesMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }

    @Bean
    public MapperFactoryBean<DynamicTableMapper> dynamicTableMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<DynamicTableMapper> factoryBean = new MapperFactoryBean<>(DynamicTableMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }

    @Bean
    public MapperFactoryBean<HissFormCategoryMapper> hissFormCategoryMapper(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<HissFormCategoryMapper> factoryBean = new MapperFactoryBean<>(HissFormCategoryMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
}
