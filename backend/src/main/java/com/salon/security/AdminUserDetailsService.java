package com.salon.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salon.auth.entity.Admin;
import com.salon.auth.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security 用户详情加载服务
 * <p>
 * 从数据库 admin 表加载管理员账号信息，供 DaoAuthenticationProvider 进行密码校验。
 * 角色以 ROLE_ 前缀存入 GrantedAuthority，配合 @PreAuthorize("hasRole('ADMIN')") 使用。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminMapper adminMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));

        if (admin == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        return User.withUsername(admin.getUsername())
                .password(admin.getPassword())
                .roles(admin.getRole()) // Spring Security 自动加 ROLE_ 前缀
                .build();
    }
}
