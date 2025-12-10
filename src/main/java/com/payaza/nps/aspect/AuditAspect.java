package com.payaza.nps.aspect;

import com.payaza.nps.annotation.Auditable;
import com.payaza.nps.model.AuditLog;
import com.payaza.nps.security.ClientContext;
import com.payaza.nps.service.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * AOP Aspect for automatic audit logging
 */
@Aspect
@Component
@Order(1)
public class AuditAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);
    
    @Autowired
    private AuditService auditService;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();
        String clientId = ClientContext.getCurrentClientId();
        String userId = getCurrentUserId();
        
        Object result = null;
        Exception exception = null;
        
        try {
            // Execute the method
            result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log success if not only logging on error
            if (!auditable.logOnlyOnError()) {
                String message = auditable.message().isEmpty() ? 
                    "Method executed successfully" : auditable.message();
                
                Map<String, Object> details = buildAuditDetails(joinPoint, auditable, result, null, executionTime);
                
                auditService.logSuccess(
                    auditable.action(),
                    auditable.resource(),
                    auditable.actionType(),
                    userId,
                    clientId,
                    message,
                    details
                );
            }
            
            return result;
            
        } catch (Exception e) {
            exception = e;
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log error
            String message = auditable.message().isEmpty() ? 
                "Method execution failed: " + e.getMessage() : auditable.message();
            
            Map<String, Object> details = buildAuditDetails(joinPoint, auditable, null, e, executionTime);
            
            auditService.logError(
                auditable.action(),
                auditable.resource(),
                auditable.actionType(),
                userId,
                clientId,
                message,
                e.getClass().getSimpleName(),
                e.getMessage(),
                details
            );
            
            throw e;
        }
    }

    /**
     * Audit all controller methods automatically
     */
    @Around("execution(* com.payaza.nps.controller.*Controller.*(..)) && " +
            "(@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping))")
    public Object auditControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String clientId = ClientContext.getCurrentClientId();
        String userId = getCurrentUserId();
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        String className = method.getDeclaringClass().getSimpleName();
        
        // Determine action type based on method name
        AuditLog.ActionType actionType = determineActionType(methodName);
        
        // Get endpoint information
        String endpoint = getEndpointInfo(method);
        
        Object result = null;
        Exception exception = null;
        
        try {
            result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            String action = className + "." + methodName;
            String resource = endpoint;
            String message = "API call completed successfully";
            
            Map<String, Object> details = Map.of(
                "className", className,
                "methodName", methodName,
                "endpoint", endpoint,
                "executionTimeMs", executionTime,
                "parameters", getMethodParameters(joinPoint)
            );
            
            auditService.logSuccess(action, resource, actionType, userId, clientId, message, details);
            
            return result;
            
        } catch (Exception e) {
            exception = e;
            long executionTime = System.currentTimeMillis() - startTime;
            
            String action = className + "." + methodName;
            String resource = endpoint;
            String message = "API call failed: " + e.getMessage();
            
            Map<String, Object> details = Map.of(
                "className", className,
                "methodName", methodName,
                "endpoint", endpoint,
                "executionTimeMs", executionTime,
                "error", e.getClass().getSimpleName(),
                "errorMessage", e.getMessage(),
                "parameters", getMethodParameters(joinPoint)
            );
            
            auditService.logError(action, resource, actionType, userId, clientId, message, 
                                e.getClass().getSimpleName(), e.getMessage(), details);
            
            throw e;
        }
    }

    private Map<String, Object> buildAuditDetails(ProceedingJoinPoint joinPoint, Auditable auditable, 
                                                  Object result, Exception exception, long executionTime) {
        Map<String, Object> details = new HashMap<>();
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        details.put("className", signature.getDeclaringType().getSimpleName());
        details.put("methodName", signature.getName());
        details.put("executionTimeMs", executionTime);
        
        if (auditable.includeParameters()) {
            details.put("parameters", getMethodParameters(joinPoint));
        }
        
        if (auditable.includeReturnValue() && result != null) {
            details.put("returnValue", result.toString());
        }
        
        if (exception != null) {
            details.put("error", exception.getClass().getSimpleName());
            details.put("errorMessage", exception.getMessage());
            details.put("stackTrace", exception.getStackTrace());
        }
        
        return details;
    }

    private AuditLog.ActionType determineActionType(String methodName) {
        if (methodName.startsWith("create") || methodName.startsWith("add") || methodName.startsWith("save")) {
            return AuditLog.ActionType.CREATE;
        } else if (methodName.startsWith("get") || methodName.startsWith("find") || methodName.startsWith("list") || methodName.startsWith("search")) {
            return AuditLog.ActionType.READ;
        } else if (methodName.startsWith("update") || methodName.startsWith("modify") || methodName.startsWith("edit")) {
            return AuditLog.ActionType.UPDATE;
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return AuditLog.ActionType.DELETE;
        } else if (methodName.contains("login") || methodName.contains("authenticate")) {
            return AuditLog.ActionType.LOGIN;
        } else if (methodName.contains("logout")) {
            return AuditLog.ActionType.LOGOUT;
        } else {
            return AuditLog.ActionType.API_CALL;
        }
    }

    private String getEndpointInfo(Method method) {
        try {
            RequestMapping mapping = method.getDeclaringClass().getAnnotation(RequestMapping.class);
            String basePath = mapping != null ? String.join("", mapping.value()) : "";
            
            // Try to find method-level mapping
            // Check for mapping annotations
            if (method.isAnnotationPresent(org.springframework.web.bind.annotation.GetMapping.class)) {
                String[] paths = method.getAnnotation(org.springframework.web.bind.annotation.GetMapping.class).value();
                if (paths.length > 0) {
                    return basePath + paths[0];
                }
            }
            if (method.isAnnotationPresent(org.springframework.web.bind.annotation.PostMapping.class)) {
                String[] paths = method.getAnnotation(org.springframework.web.bind.annotation.PostMapping.class).value();
                if (paths.length > 0) {
                    return basePath + paths[0];
                }
            }
            if (method.isAnnotationPresent(org.springframework.web.bind.annotation.PutMapping.class)) {
                String[] paths = method.getAnnotation(org.springframework.web.bind.annotation.PutMapping.class).value();
                if (paths.length > 0) {
                    return basePath + paths[0];
                }
            }
            if (method.isAnnotationPresent(org.springframework.web.bind.annotation.DeleteMapping.class)) {
                String[] paths = method.getAnnotation(org.springframework.web.bind.annotation.DeleteMapping.class).value();
                if (paths.length > 0) {
                    return basePath + paths[0];
                }
            }
            if (method.isAnnotationPresent(org.springframework.web.bind.annotation.PatchMapping.class)) {
                String[] paths = method.getAnnotation(org.springframework.web.bind.annotation.PatchMapping.class).value();
                if (paths.length > 0) {
                    return basePath + paths[0];
                }
            }
            
            return basePath + "/" + method.getName();
        } catch (Exception e) {
            return method.getName();
        }
    }

    private Map<String, Object> getMethodParameters(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        
        Map<String, Object> parameters = new HashMap<>();
        for (int i = 0; i < args.length && i < paramNames.length; i++) {
            // Don't log sensitive data
            if (!isSensitiveParameter(paramNames[i])) {
                parameters.put(paramNames[i], args[i]);
            } else {
                parameters.put(paramNames[i], "[REDACTED]");
            }
        }
        return parameters;
    }

    private boolean isSensitiveParameter(String paramName) {
        String lowerParamName = paramName.toLowerCase();
        return lowerParamName.contains("password") || 
               lowerParamName.contains("secret") || 
               lowerParamName.contains("key") || 
               lowerParamName.contains("token") ||
               lowerParamName.contains("auth");
    }

    private String getCurrentUserId() {
        // Try to get current user from security context
        try {
            // This would need to be implemented based on your authentication mechanism
            return "CURRENT_USER"; // Placeholder
        } catch (Exception e) {
            return null;
        }
    }
}
