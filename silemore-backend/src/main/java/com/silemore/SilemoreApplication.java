package com.silemore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SilemoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(SilemoreApplication.class, args);
    }
}
