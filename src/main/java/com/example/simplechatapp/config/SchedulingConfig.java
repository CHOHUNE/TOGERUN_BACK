package com.example.simplechatapp.config;


import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
@EnableAsync //Async : 비동기 처리를 위한 어노테이션
@Log4j2
public class SchedulingConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2); //스케줄러가 사용할 쓰레드 개수
        scheduler.setThreadNamePrefix("scheduled-"); //스케줄러가 사용할 쓰레드 이름 접두어
        scheduler.setErrorHandler(t-> log.error("Scheduled task error : {}",t)); //스케줄러 에러 핸들러

        return scheduler;
    }
}
