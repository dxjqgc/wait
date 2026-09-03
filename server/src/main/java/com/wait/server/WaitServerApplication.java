package com.wait.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wait.server.mapper")
public class WaitServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WaitServerApplication.class, args);
    }
}
