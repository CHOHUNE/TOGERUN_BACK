package com.example.simplechatapp.aop;

import com.example.simplechatapp.annotation.DistributedLock;
import com.example.simplechatapp.service.RedisLockService;
import com.example.simplechatapp.util.LockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {
    private final RedisLockService lockService;
    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(distributedLock)")
    public Object executeWithLock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = generateLockKey(joinPoint, distributedLock);

        try {
            if (!lockService.acquireLock(lockKey, distributedLock.waitTime())) {
                throw new LockException.AcquisitionException(lockKey);
            }
            return joinPoint.proceed();
        } finally {
            try {
                lockService.releaseLock(lockKey);
            } catch (Exception e) {
                log.error("Failed to release lock for key: {}", lockKey, e);
                throw new LockException.ReleaseException(lockKey);
            }
        }
    }

    private String generateLockKey(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String baseKey = method.getDeclaringClass().getSimpleName() + ":" + method.getName() + ":";

        EvaluationContext context = new StandardEvaluationContext();

        Object[] args = joinPoint.getArgs();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            context.setVariable(parameters[i].getName(), args[i]);
        }

        String expressionValue = parser.parseExpression(distributedLock.key())
                .getValue(context, String.class);

        return baseKey + expressionValue;
    }
}