package com.fitness.tracking_service.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAdvice {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Pointcut for all methods in service package
    @Pointcut("execution(* com.fitness.tracking_service.service..*(..))")
    public void serviceLayerMethods() {}

    // Pointcut for all methods in controller package
    @Pointcut("execution(* com.fitness.tracking_service.controllers..*(..))")
    public void controllerLayerMethods() {}

    // Pointcut for all methods in repository package
    @Pointcut("execution(* com.fitness.tracking_service.repository..*(..))")
    public void repositoryLayerMethods() {}

    @Around("serviceLayerMethods() || controllerLayerMethods()")
    public Object logAroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] methodArgs = joinPoint.getArgs();

        // Log before method execution
        log.info("======================== START ========================");
        log.info("Class: {}", className);
        log.info("Method: {}", methodName);

        if (methodArgs != null && methodArgs.length > 0) {
            log.info("Parameters:");
            for (int i = 0; i < methodArgs.length; i++) {
                try {
                    String argValue = objectMapper.writeValueAsString(methodArgs[i]);
                    log.info("  Arg[{}]: {}", i, argValue);
                } catch (Exception e) {
                    log.info("  Arg[{}]: {} (toString)", i, methodArgs[i]);
                }
            }
        } else {
            log.info("Parameters: None");
        }

        long startTime = System.currentTimeMillis();
        Object result = null;

        try {
            // Execute the actual method
            result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;

            // Log after successful execution
            log.info("Execution Status: SUCCESS");
            log.info("Execution Time: {} ms", executionTime);

            if (result != null) {
                try {
                    String resultValue = objectMapper.writeValueAsString(result);
                    log.info("Return Value: {}", resultValue);
                } catch (Exception e) {
                    log.info("Return Value: {} (toString)", result);
                }
            } else {
                log.info("Return Value: null");
            }

            log.info("========================= END =========================");

            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            // Log after failed execution
            log.error("Execution Status: FAILED");
            log.error("Execution Time: {} ms", executionTime);
            log.error("Exception Type: {}", e.getClass().getSimpleName());
            log.error("Exception Message: {}", e.getMessage());
            log.info("========================= END =========================");

            throw e;
        }
    }
}

