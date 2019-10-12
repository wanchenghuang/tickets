package com.track.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @Author: HUANGWANCHENG
 * @Date: 2019/09/01 11:30
 * @Version 1.0
 */

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class/*, MongoAutoConfiguration.class*/})   // 排除自定注入的bean
@MapperScan("com.track.*.mapper.*")
@ComponentScan(basePackages = {"com.track"})
@EnableSwagger2
@EnableRedisRepositories
@EnableRabbit
//@EnableAspectJAutoProxy //aop编程
public class StartApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(StartApplication.class, args);
        System.out.println("启动成功");
    }


}
