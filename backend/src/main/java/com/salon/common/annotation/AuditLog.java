package com.salon.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解
 * <p>
 * 标注在 Controller 方法上，自动记录操作日志到数据库。
 * 支持自定义操作描述，可用 SpEL 表达式引用方法参数。
 * </p>
 *
 * <pre>
 * 示例：
 * &#64;AuditLog("删除会员，ID: #{#id}")
 * public Result<Void> deleteMember(@PathVariable Long id) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /** 操作描述，支持 SpEL 表达式 */
    String value() default "";
}
