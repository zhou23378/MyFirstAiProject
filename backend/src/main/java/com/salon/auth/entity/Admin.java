package com.salon.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员实体
 * <p>
 * 存储管理员账号信息，用于登录认证。继承 BaseEntity 统一管理 id/createTime/updateTime。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin")
@Schema(description = "管理员实体")
public class Admin extends BaseEntity {

    /** 用户名，唯一 */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /** 密码（BCrypt 加密） */
    @Schema(description = "密码（BCrypt加密）")
    private String password;

    /** 角色 */
    @Schema(description = "角色", example = "ADMIN")
    private String role;
}
