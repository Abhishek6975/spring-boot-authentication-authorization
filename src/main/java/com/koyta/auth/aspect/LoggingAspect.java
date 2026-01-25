package com.koyta.auth.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;


/*
@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @Around("execution(* com.koyta.controllers..*(..))")
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

    @Around("execution(* com.koyta.services..*(..))")
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

 */

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.koyta.controllers..*(..)) || execution(* com.koyta.services..*(..))")
    public Object logReactiveMethods(ProceedingJoinPoint joinPoint) throws Throwable {

        Signature signature = joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        // REACTIVE RETURN TYPE
        if (result instanceof reactor.core.publisher.Mono<?> mono) {

            return mono
                    .doOnSubscribe(sub ->
                            log.info("Invoking -> Class: {}, Method: {}()", className, methodName)
                    )
                    .doFinally(signal -> {
                        long duration = System.currentTimeMillis() - startTime;
                        log.info(
                                "Execution completed -> Class: {}, Method: {}, Duration: {} ms, Signal: {}",
                                className,
                                methodName,
                                duration,
                                signal
                        );
                    });
        }

        if (result instanceof reactor.core.publisher.Flux<?> flux) {

            return flux
                    .doOnSubscribe(sub ->
                            log.info("Invoking -> Class: {}, Method: {}()", className, methodName)
                    )
                    .doFinally(signal -> {
                        long duration = System.currentTimeMillis() - startTime;
                        log.info(
                                "Execution completed -> Class: {}, Method: {}, Duration: {} ms, Signal: {}",
                                className,
                                methodName,
                                duration,
                                signal
                        );
                    });
        }

        // NON-REACTIVE METHOD (utility / sync)
        long duration = System.currentTimeMillis() - startTime;
        log.info(
                "Execution completed -> Class: {}, Method: {}, Duration: {} ms",
                className,
                methodName,
                duration
        );

        return result;
    }
}

