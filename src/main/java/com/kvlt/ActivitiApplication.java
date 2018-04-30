package com.kvlt;

import com.kvlt.activiti.config.explorer.JsonpCallbackFilter;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author daishengkai
 * 2018-04-21 10:10
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class
})
@Configuration
@ComponentScan({"org.activiti.rest.diagram", "com.kvlt"})
@EnableTransactionManagement
@MapperScan(value = {"com.kvlt.mapper"})
@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration.class,
        org.activiti.spring.boot.SecurityAutoConfiguration.class,
        org.springframework.boot.actuate.autoconfigure.ManagementWebSecurityAutoConfiguration.class  //  activiti-rest 屏蔽authentication
})
@EnableAsync
public class ActivitiApplication {

    @Bean
    public JsonpCallbackFilter filter() {
        return new JsonpCallbackFilter();
    }

    public static void main(String[] args) {
        SpringApplication.run(ActivitiApplication.class, args);
    }

}
