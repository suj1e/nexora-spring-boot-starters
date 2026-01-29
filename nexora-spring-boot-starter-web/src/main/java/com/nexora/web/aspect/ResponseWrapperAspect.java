package com.nexora.web.aspect;

import com.nexora.web.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Response wrapper aspect.
 *
 * <p>Automatically wraps all controller return values in {@link Result} format.
 *
 * <p>Usage: Controller returns entity directly, AOP wraps it automatically.
 * <pre>
 * // Controller - NO wrapping needed
 * &#64;GetMapping("/users/{id}")
 * public User getUser(@PathVariable Long id) {
 *     return userService.findById(id);  // Return entity directly
 * }
 *
 * // Client receives:
 * {
 *   "code": 200,
 *   "message": "success",
 *   "data": { "id": 1, "username": "john" },
 *   "timestamp": "2024-01-28T10:00:00Z"
 * }
 * </pre>
 *
 * @author sujie
 */
@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ResponseWrapperAspect {

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object wrapResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        // If already wrapped or void, return as-is
        if (result == null || result instanceof Result || result instanceof Comparable<?> || result instanceof String) {
            return result;
        }

        // Automatically wrap in Result
        return Result.success(result);
    }
}
