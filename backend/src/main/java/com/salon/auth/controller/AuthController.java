package com.salon.auth.controller;

import com.salon.auth.dto.LoginRequest;
import com.salon.common.annotation.AuditLog;
import com.salon.auth.dto.LoginResponse;
import com.salon.auth.service.AdminService;
import com.salon.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证 Controller
 * <p>
 * 提供管理员登录认证接口
 * </p>
 */
@Tag(name = "认证管理", description = "管理员登录认证接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AdminService adminService;

    /**
     * 管理员登录
     * <p>
     * 验证用户名和密码，成功后返回 JWT Token
     * </p>
     *
     * @param req 登录请求（用户名、密码）
     * @return 登录响应（Token、用户名）
     */
    @AuditLog("管理员登录 #{req.username}")
    @Operation(summary = "管理员登录", description = "验证用户名密码，返回JWT Token")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.success(adminService.login(req));
    }
}
