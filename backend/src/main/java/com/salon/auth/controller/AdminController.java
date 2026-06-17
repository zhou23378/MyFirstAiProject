package com.salon.auth.controller;

import com.salon.auth.dto.AdminReqDTO;
import com.salon.auth.entity.Admin;
import com.salon.auth.mapper.AdminMapper;
import com.salon.auth.vo.AdminVO;
import com.salon.common.annotation.AuditLog;
import com.salon.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理员管理", description = "管理员账号的增删改查")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @Operation(summary = "获取全部管理员")
    public Result<List<AdminVO>> list() {
        return Result.success(adminMapper.selectList(null).stream()
                .map(AdminVO::from)
                .toList());
    }

    @AuditLog("新增管理员 #{entity.username}")
    @PostMapping
    @Operation(summary = "新增管理员")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<AdminVO> create(@Valid @RequestBody AdminReqDTO dto) {
        Admin entity = new Admin();
        entity.setUsername(dto.getUsername());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity.setRole(dto.getRole());
        adminMapper.insert(entity);
        entity.setPassword(null);
        return Result.success(AdminVO.from(entity));
    }

    @AuditLog("修改管理员角色 ID=#{id}")
    @PutMapping("/{id}")
    @Operation(summary = "修改管理员（角色等）")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<AdminVO> update(@PathVariable Long id, @Valid @RequestBody AdminReqDTO dto) {
        Admin existing = adminMapper.selectById(id);
        if (existing == null) return Result.notFound("管理员不存在");
        existing.setRole(dto.getRole());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        adminMapper.updateById(existing);
        existing.setPassword(null);
        return Result.success(AdminVO.from(existing));
    }

    @AuditLog("删除管理员 ID=#{id}")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除管理员")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        adminMapper.deleteById(id);
        return Result.success();
    }
}
