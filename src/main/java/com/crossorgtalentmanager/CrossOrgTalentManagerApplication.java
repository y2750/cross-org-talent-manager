package com.crossorgtalentmanager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.crossorgtalentmanager.mapper")
public class CrossOrgTalentManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrossOrgTalentManagerApplication.class, args);
    }

}
