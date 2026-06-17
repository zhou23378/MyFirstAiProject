package com.salon.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应 DTO
 * <p>
 * 登录成功后返回 JWT Token 和用户名
 * </p>
 */
@Data
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    /** JWT Token */
    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiIs...")
    private String token;

    /** 用户名 */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /** 角色 */
    @Schema(description = "角色", example = "admin")
    private String role;
}
