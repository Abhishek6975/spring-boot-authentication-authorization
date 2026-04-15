package com.koyta.auth.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @InjectMocks
    private LoggingAspect loggingAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    //  COMMON MOCK SETUP
    private void mockJoinPoint(String className, String methodName, Object returnValue) throws Throwable {

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn((Class) Class.forName("java.lang.String")); // dummy
        when(signature.getName()).thenReturn(methodName);
        when(joinPoint.proceed()).thenReturn(returnValue);
    }

    //  1. Controller success flow
    @Test
    void shouldExecuteControllerAspect() throws Throwable {

        mockJoinPoint("TestController", "testMethod", "result");

        Object result = loggingAspect.JointPointController(joinPoint);

        assertEquals("result", result);
        verify(joinPoint).proceed();
    }

    // 2. Service success flow
    @Test
    void shouldExecuteServiceAspect() throws Throwable {

        mockJoinPoint("TestService", "serviceMethod", "response");

        Object result = loggingAspect.JointPointService(joinPoint);

        assertEquals("response", result);
        verify(joinPoint).proceed();
    }

    // 3. Exception flow (VERY IMPORTANT)
    @Test
    void shouldThrowExceptionFromControllerAspect() throws Throwable {

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn((Class) Class.forName("java.lang.String"));
        when(signature.getName()).thenReturn("failMethod");

        when(joinPoint.proceed()).thenThrow(new RuntimeException("error"));

        assertThrows(RuntimeException.class,
                () -> loggingAspect.JointPointController(joinPoint));
    }

    // 4. Exception flow for service
    @Test
    void shouldThrowExceptionFromServiceAspect() throws Throwable {

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn((Class) Class.forName("java.lang.String"));
        when(signature.getName()).thenReturn("failService");

        when(joinPoint.proceed()).thenThrow(new RuntimeException("error"));

        assertThrows(RuntimeException.class,
                () -> loggingAspect.JointPointService(joinPoint));
    }
}
