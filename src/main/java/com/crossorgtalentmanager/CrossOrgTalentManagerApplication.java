package com.crossorgtalentmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)

public class CrossOrgTalentManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrossOrgTalentManagerApplication.class, args);
    }

}
