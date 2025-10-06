package com.payaza.nps.annotation;

import com.payaza.nps.model.AuditLog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for automatic audit logging
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    
    /**
     * The action being performed
     */
    String action();
    
    /**
     * The resource being acted upon
     */
    String resource();
    
    /**
     * The type of action
     */
    AuditLog.ActionType actionType();
    
    /**
     * Whether to include method parameters in audit details
     */
    boolean includeParameters() default true;
    
    /**
     * Whether to include return value in audit details
     */
    boolean includeReturnValue() default false;
    
    /**
     * Custom message for the audit log
     */
    String message() default "";
    
    /**
     * Whether to log only on errors
     */
    boolean logOnlyOnError() default false;
}
