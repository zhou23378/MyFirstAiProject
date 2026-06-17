package com.salon.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.salon.audit.entity.AuditLogEntry;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志 Mapper
 * <p>
 * 使用 MyBatis Plus BaseMapper 提供基本 CRUD。
 * 批量归档/清理操作在 Service 层实现。
 * </p>
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLogEntry> {
}
