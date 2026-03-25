package com.jiaotou.bigmodel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BigModelApplication {
    public static void main(String[] args) {
        SpringApplication.run(BigModelApplication.class, args);
    }
}
