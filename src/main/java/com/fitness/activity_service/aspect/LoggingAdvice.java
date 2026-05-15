package com.fitness.activity_service.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAdvice {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Pointcut for all methods in the controller package
     */
    @Pointcut("execution(* com.fitness.activity_service.controllers..*(..))")
    public void controllerPointcut() {}

    /**
     * Pointcut for all methods in the service package
     */
    @Pointcut("execution(* com.fitness.activity_service.service..*(..))")
    public void servicePointcut() {}

    /**
     * Pointcut for all methods in the repository package
     */
    @Pointcut("execution(* com.fitness.activity_service.repository..*(..))")
    public void repositoryPointcut() {}

    /**
     * Around advice that logs method entry, exit, and execution time
     */
    @Around("controllerPointcut() || servicePointcut() || repositoryPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("==> Entering method: {}.{}() with arguments: {}",
                className, methodName, formatArgs(args));

        long startTime = System.currentTimeMillis();
        Object result;

        try {
            result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            log.info("<== Method: {}.{}() executed in {} ms with result: {}",
                    className, methodName, executionTime, formatResult(result));

            return result;
        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("<== Method: {}.{}() threw exception after {} ms: {}",
                    className, methodName, executionTime, throwable.getMessage());
            throw throwable;
        }
    }

    /**
     * After throwing advice for exception logging
     */
    @AfterThrowing(pointcut = "controllerPointcut() || servicePointcut() || repositoryPointcut()",
                   throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        log.error("Exception in {}.{}() with cause: {} and message: {}",
                className, methodName,
                exception.getCause() != null ? exception.getCause() : "NULL",
                exception.getMessage());
    }

    /**
     * Format method arguments for logging
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            return Arrays.toString(args);
        }
    }

    /**
     * Format method result for logging
     */
    private String formatResult(Object result) {
        if (result == null) {
            return "null";
        }
        try {
            String json = objectMapper.writeValueAsString(result);
            // Truncate if too long
            if (json.length() > 500) {
                return json.substring(0, 500) + "... [truncated]";
            }
            return json;
        } catch (Exception e) {
            return result.toString();
        }
    }
}
