package com.salon.audit.service;

import com.salon.audit.entity.AuditLogEntry;
import com.salon.audit.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 审计日志异步写入服务
 * <p>
 * 独立 Service 确保 @Async 通过 Spring AOP 代理生效，
 * 避免 AuditLogAspect 内部 self-invocation 绕过代理的问题。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogAsyncService {

    private final AuditLogMapper auditLogMapper;

    @Async("auditExecutor")
    public void save(AuditLogEntry entry) {
        try {
            auditLogMapper.insert(entry);
        } catch (Exception e) {
            log.error("异步保存审计日志失败", e);
        }
    }
}
