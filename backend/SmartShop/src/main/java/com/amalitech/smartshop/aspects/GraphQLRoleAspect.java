package com.amalitech.smartshop.aspects;

import com.amalitech.smartshop.config.GraphQLRequiresRole;
import com.amalitech.smartshop.enums.UserRole;
import com.amalitech.smartshop.exceptions.UnauthorizedException;
import graphql.schema.DataFetchingEnvironment;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * Aspect for role-based authorization in GraphQL operations.
 */
@Aspect
@Component
public class GraphQLRoleAspect {

    @Around("@annotation(com.amalitech.smartshop.config.GraphQLRequiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        // I validate the user's role for GraphQL operations
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        GraphQLRequiresRole annotation = signature.getMethod().getAnnotation(GraphQLRequiresRole.class);

        DataFetchingEnvironment env = null;
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof DataFetchingEnvironment) {
                env = (DataFetchingEnvironment) arg;
                break;
            }
        }

        if (env == null) {
            throw new UnauthorizedException("Authentication required");
        }

        String userRole = env.getGraphQlContext().get("userRole");
        if (userRole == null) {
            throw new UnauthorizedException("Authentication required");
        }

        for (UserRole requiredRole : annotation.value()) {
            if (requiredRole.name().equals(userRole)) {
                return joinPoint.proceed();
            }
        }

        throw new UnauthorizedException("User does not have required role");
    }
}
