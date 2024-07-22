package com.example.simplechatapp.aop;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Slf4j
@Profile("local") // local 환경에서만 나타나도록 정의
@Component // 빈으로 등록
public class LogAspect {

    //포인트 컷 : 어떤 메서드에 적용할 것인지 정의
    // 백엔드 모두
    @Pointcut("execution(* com.example.simplechatapp..*(..))")
    public void all() {

    }


    // 모든 컨트롤러
    @Pointcut("execution(* com.example.simplechatapp.controller.*(..))") // ..*: 하위 패키지까지 포함, .*: 메서드까지 포함
    public void controller() {

    }

    //모든 서비스
    @Pointcut("execution(* com.example.simplechatapp.service.*(..))")
    public void service() {

    }

    // 메서드 호출 전/후 예외발생 시점에 해당 부가기능 (Advice) 실행
    @Around("all()") // 인자로 위의 all() 포인트 컷 삽입
    public Object logging (ProceedingJoinPoint joinPoint) throws Throwable{
//ProceedingJointPoint : jointPoint 를 확장한 것 ( 실제 메소드 실행을 직접 제어하는 기능을 제공하는 객체 )
        long start = System.currentTimeMillis();

        try{
            Object result = joinPoint.proceed();

             // jointPoint : advice 가 적용되는 시점에서 메소드 실행 정보를 제공하는 인터페이스
            // proceed 는 advice 가 실행 후 그 결과 값을 반환한다.

            return result;

        }finally {

            long finish = System.currentTimeMillis();
            long timeMs = finish - start;

            log.info(" log = {} ", joinPoint.getSignature()); // 현재 advice 가 적용된 메서드의 시그니처를 가져온다
            // 시그니처는 메서드명, 매개변수 등
            log.info(" timeMs = {}", timeMs);
        }
    }

    @Before("controller() || service() ")
    // beforeLogic :  advice 가 적용된 메서드 실행 전 , 메서드의 이름, 인자값을 로그로 출력
    public void beforeLogic(JoinPoint joinPoint) throws Throwable {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        // 현재 advice 가 적용된 메서드의 시그니처를 가져온다
        // 메서드 시그니처는 메서드명, 매개변수 등의 정보
        Method method = methodSignature.getMethod();
        // 메서드 시그니처로 부터 메서드 얻기


        log.info("method = {}", method.getName());

        Object[] args = joinPoint.getArgs(); // jointPoint 를 통해 메서드의 인자값을 가져온다
        for (Object arg : args) {

            if (arg == null) {

                log.info(" type ={} ", arg.getClass().getSimpleName());
                log.info(" value = {} ", arg);
            }
        }
    }


    @After("controller() || service()") // 컨트롤러, 서비스가 실행된 직후에 부가기능 실행 ( advice 실행 )
    public void afterLogic(JoinPoint joinPoint) throws Throwable {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        log.info(" method = {}", method.getName());

        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {

            if (arg != null) {

                log.info("type = {}", arg.getClass().getSimpleName());
                log.info("value = {}", arg);
            }
         }
    }
}
