package com.kvlt.activiti.config;

import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.AbstractProcessEngineAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;

/**
 * @author daishengkai
 * 2018-04-28 10:40
 */
@ComponentScan
@Configuration   // 声明为配置类，集成Activiti抽象配置类
public class ActivitiConfig extends AbstractProcessEngineAutoConfiguration {

//    @Resource
//    DataSource activitiDataSource;  // 注入配置好的数据源

//    @Resource
//    PlatformTransactionManager activitiTransactionManager;  // 注入配置好的事务管理器

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.activiti")
    public DataSource activitiDataSource() {
        return DataSourceBuilder.create().build();
    }

    //注入数据源和事务管理器
    @Bean
    public SpringProcessEngineConfiguration springProcessEngineConfiguration(PlatformTransactionManager activitiTransactionManager, SpringAsyncExecutor springAsyncExecutor) throws IOException {
        return this.baseSpringProcessEngineConfiguration(activitiDataSource(), activitiTransactionManager, springAsyncExecutor);
    }

}
