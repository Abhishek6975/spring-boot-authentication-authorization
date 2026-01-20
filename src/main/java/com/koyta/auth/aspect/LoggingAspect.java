package com.koyta.auth.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @Around("execution(* com.techpluse.controller..*(..))")
    public Object JointPointController(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        String ClassName = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        log.info("Invoking -> Class: {}, Method: {}()", ClassName, methodName);

        long StartTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long AppExectionTime = System.currentTimeMillis() - StartTime;

        log.info("Execution completed -> Class: {}, Method: {}, Duration: {} ms", ClassName, methodName,
                AppExectionTime);

        return result;
    }

    @Around("execution(* com.techpluse.service..*(..))")
    public Object JointPointService(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        String ClassName = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        log.info("Invoking -> Class: {}, Method: {}()", ClassName, methodName);

        long StartTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long AppExectionTime = System.currentTimeMillis() - StartTime;

        log.info("Execution completed -> Class: {}, Method: {}, Duration: {} ms", ClassName, methodName,
                AppExectionTime);

        return result;
    }
}
