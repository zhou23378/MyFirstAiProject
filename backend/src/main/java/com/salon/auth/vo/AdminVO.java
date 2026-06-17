package com.salon.auth.vo;

import com.salon.auth.entity.Admin;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理员视图（不含密码）")
public class AdminVO {

    public static AdminVO from(Admin entity) {
        if (entity == null) return null;
        AdminVO vo = new AdminVO();
        BeanUtils.copyProperties(entity, vo, "password");
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
