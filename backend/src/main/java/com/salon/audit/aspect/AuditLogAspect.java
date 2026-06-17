package com.salon.audit.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salon.audit.entity.AuditLogEntry;
import com.salon.audit.service.AuditLogAsyncService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 审计日志 AOP 切面
 * <p>
 * 拦截所有标注了 @AuditLog 的方法，自动记录操作日志到数据库。
 * 关键特性：
 * - 异步写入（@Async），不阻塞业务请求
 * - 自动脱敏手机号
 * - 记录请求耗时
 * - 捕获操作人信息（从 Request Attribute 中获取）
 * </p>
 */
@Slf4j
@Aspect
@Component
public class AuditLogAspect {

    private final AuditLogAsyncService auditLogAsyncService;
    private final ObjectMapper objectMapper;

    /** 手机号脱敏正则：138****1234 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("(1[3-9]\\d)\\d{4}(\\d{4})");

    public AuditLogAspect(AuditLogAsyncService auditLogAsyncService, ObjectMapper objectMapper) {
        this.auditLogAsyncService = auditLogAsyncService;
        this.objectMapper = objectMapper;
    }

    /**
     * 环绕通知：拦截 @AuditLog 方法
     */
    @Around("@annotation(com.salon.common.annotation.AuditLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String errorMessage = null;

        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            status = "FAILED";
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            try {
                AuditLogEntry entry = buildLogEntry(joinPoint, status, errorMessage, costTime);
                auditLogAsyncService.save(entry);
            } catch (Exception e) {
                log.error("保存审计日志失败", e);
            }
        }
    }

    /**
     * 构建审计日志实体
     */
    private AuditLogEntry buildLogEntry(ProceedingJoinPoint joinPoint,
                                 String status, String errorMessage, long costTime) {
        try {
            AuditLogEntry logEntry = new AuditLogEntry();

            // 操作人信息（从 Request Attribute 获取）
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                logEntry.setUsername(Optional.ofNullable(request.getAttribute("username"))
                        .map(Object::toString).orElse("unknown"));
                logEntry.setRole(Optional.ofNullable(request.getAttribute("role"))
                        .map(Object::toString).orElse("unknown"));
            }

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            // 操作描述
            com.salon.common.annotation.AuditLog annotation =
                    method.getAnnotation(com.salon.common.annotation.AuditLog.class);
            logEntry.setAction(getActionDescription(annotation, joinPoint));

            // HTTP 请求信息
            if (request != null) {
                logEntry.setMethod(request.getMethod());
                logEntry.setUrl(request.getRequestURI());
                logEntry.setIp(getClientIp(request));
            } else {
                logEntry.setMethod("INTERNAL");
                logEntry.setUrl(method.getDeclaringClass().getSimpleName() + "." + method.getName());
                logEntry.setIp("127.0.0.1");
            }

            // 请求参数（脱敏）
            logEntry.setParams(maskSensitiveParams(joinPoint.getArgs()));

            logEntry.setStatus(status);
            logEntry.setErrorMessage(errorMessage);
            logEntry.setCostTime(costTime);
            logEntry.setCreateTime(LocalDateTime.now());

            return logEntry;
        } catch (Exception e) {
            log.error("构建审计日志实体异常", e);
            return null;
        }
    }

    /**
     * 解析操作描述，支持简易 SpEL 表达式
     */
    private String getActionDescription(com.salon.common.annotation.AuditLog annotation,
                                         ProceedingJoinPoint joinPoint) {
        String value = annotation.value();
        if (value == null || value.isBlank()) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            return signature.getMethod().getName();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        String result = value;
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                String placeholder = "#{" + paramNames[i] + "}";
                if (result.contains(placeholder) && args[i] != null) {
                    result = result.replace(placeholder, args[i].toString());
                }
            }
        }
        return result;
    }

    /**
     * 脱敏处理：手机号中间4位替换为 ****
     */
    private String maskSensitiveParams(Object[] args) {
        try {
            if (args == null || args.length == 0) {
                return "";
            }

            // 过滤掉 HttpServletRequest/HttpServletResponse 等不可序列化对象
            Object[] filteredArgs = java.util.Arrays.stream(args)
                    .filter(arg -> !(arg instanceof HttpServletRequest))
                    .filter(arg -> !(arg instanceof HttpServletResponse))
                    .toArray();

            if (filteredArgs.length == 0) {
                return "";
            }

            String json = objectMapper.writeValueAsString(filteredArgs);

            // 脱敏手机号
            java.util.regex.Matcher matcher = PHONE_PATTERN.matcher(json);
            json = matcher.replaceAll("$1****$2");

            // 截断过长参数（> 2000 字符）
            if (json.length() > 2000) {
                json = json.substring(0, 2000) + "...(truncated)";
            }

            return json;
        } catch (Exception e) {
            return "[序列化失败]";
        }
    }

    /**
     * 获取当前 HTTP 请求
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
