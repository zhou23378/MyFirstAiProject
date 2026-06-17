package com.salon.audit.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志实体
 * <p>
 * 记录系统中所有标注了 @AuditLog 注解的方法调用日志。
 * 表名: audit_log
 * </p>
 */
@Data
@TableName("audit_log")
public class AuditLogEntry {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人用户名 */
    private String username;

    /** 操作人角色 */
    private String role;

    /** 操作描述 */
    private String action;

    /** HTTP 方法 */
    private String method;

    /** 请求 URL */
    private String url;

    /** 客户端 IP */
    private String ip;

    /** 请求参数（脱敏后 JSON） */
    private String params;

    /** 执行状态：SUCCESS / FAILED */
    private String status;

    /** 错误信息（失败时记录） */
    private String errorMessage;

    /** 耗时（毫秒） */
    private Long costTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
