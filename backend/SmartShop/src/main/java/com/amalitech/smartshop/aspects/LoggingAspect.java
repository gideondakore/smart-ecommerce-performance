package com.amalitech.smartshop.aspects;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("within(com.amalitech.smartshop.controllers..*)")
    public void controllerLayer() {
    }

    @Around("controllerLayer()")
    public Object logAroundControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        log.info("HTTPS SERVLET REQUEST: " + request);
        String method = request.getMethod();
        String path = request.getRequestURI();
        String clientIp = request.getRemoteAddr();

        Object[] args = joinPoint.getArgs();
        String params = args.length > 0 ? Arrays.toString(args) : "none";

        log.info("→ {} {} | Controller: {} | Params: {} | IP: {}",
                method, path, joinPoint.getSignature().getName(), params, clientIp);

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            int status = 200;
            if (result instanceof ResponseEntity) {
                status = ((ResponseEntity<?>) result).getStatusCode().value();
            }
            
            log.info("← {} {} | Status: {} | Controller: {} | Time: {}ms",
                    method, path, status, joinPoint.getSignature().getName(), executionTime);
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            log.error("← {} {} | Controller: {} | Status: FAILED | Time: {}ms | Error: {} - {}",
                    method, path, joinPoint.getSignature().getName(), executionTime,
                    e.getClass().getSimpleName(), e.getMessage());

            throw e;
        }
    }
}
