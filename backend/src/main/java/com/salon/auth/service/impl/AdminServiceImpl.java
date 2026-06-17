package com.salon.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.auth.dto.LoginRequest;
import com.salon.auth.dto.LoginResponse;
import com.salon.auth.entity.Admin;
import com.salon.auth.mapper.AdminMapper;
import com.salon.auth.service.AdminService;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import com.salon.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 管理员 Service 实现
 * <p>
 * 实现管理员登录认证逻辑
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * 管理员登录
     * <p>
     * 1. 根据用户名查询管理员
     * 2. 使用 BCrypt 比对密码
     * 3. 生成 JWT Token 返回
     * </p>
     */
    @Override
    public LoginResponse login(LoginRequest req) {
        // 查询管理员
        Admin admin = baseMapper.selectOne(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getUsername, req.getUsername()));

        // 校验用户名密码（BCrypt）
        if (admin == null || !passwordEncoder.matches(req.getPassword(), admin.getPassword())) {
            log.warn("登录失败: username={}", req.getUsername());
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 生成 Token（携带角色信息）
        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername(), admin.getRole());
        log.info("登录成功: username={}", req.getUsername());
        return new LoginResponse(token, admin.getUsername(), admin.getRole());
    }
}
