package com.amalitech.smartshop.aspects;

import com.amalitech.smartshop.entities.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

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
        String method = request.getMethod();
        String path = request.getRequestURI();
        String clientIp = request.getRemoteAddr();

        Object[] args = joinPoint.getArgs();
        String params = args.length > 0
            ? Arrays.stream(args)
            .map(this::formatArgument)
            .toList()
            .toString()
            : "none";

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

    private String formatArgument(Object argument) {
        if (argument == null) {
            return "null";
        }

        if (argument instanceof String
                || argument instanceof Number
                || argument instanceof Boolean
                || argument instanceof Enum<?>) {
            return String.valueOf(argument);
        }

        if (argument instanceof User user) {
            return "User{id=" + user.getId() + ", email=" + user.getEmail() + "}";
        }

        if (argument instanceof UserDetails userDetails) {
            return "UserDetails{username=" + userDetails.getUsername() + "}";
        }

        if (argument instanceof Collection<?> collection) {
            return "Collection(size=" + collection.size() + ")";
        }

        if (argument instanceof Map<?, ?> map) {
            return "Map(size=" + map.size() + ")";
        }

        return argument.getClass().getSimpleName();
    }
}
