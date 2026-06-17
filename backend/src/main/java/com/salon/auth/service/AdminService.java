package com.salon.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.auth.dto.LoginRequest;
import com.salon.auth.dto.LoginResponse;
import com.salon.auth.entity.Admin;

/**
 * 管理员 Service 接口
 * <p>
 * 定义管理员登录认证的业务方法
 * </p>
 */
public interface AdminService extends IService<Admin> {

    /**
     * 管理员登录
     * <p>
     * 验证用户名密码，成功后返回 JWT Token
     * </p>
     *
     * @param req 登录请求（用户名、密码）
     * @return 登录响应（Token、用户名）
     */
    LoginResponse login(LoginRequest req);
}
