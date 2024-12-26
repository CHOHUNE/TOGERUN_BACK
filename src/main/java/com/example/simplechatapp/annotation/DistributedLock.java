package com.example.simplechatapp.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) //  메서드 단위에 붙여서 사용
@Retention(RetentionPolicy.RUNTIME)  // 런타임까지 유지
public @interface DistributedLock {

    String key();

    long waitTime() default 3L; // 기본 3초 대기


}
