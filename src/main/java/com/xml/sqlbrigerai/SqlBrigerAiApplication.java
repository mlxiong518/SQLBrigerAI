package com.xml.sqlbrigerai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.xml.sqlbrigerai")
public class SqlBrigerAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SqlBrigerAiApplication.class, args);
    }

}
