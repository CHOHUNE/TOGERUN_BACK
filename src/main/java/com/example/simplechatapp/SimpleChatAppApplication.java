package com.example.simplechatapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SimpleChatAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleChatAppApplication.class, args);
    }

}
