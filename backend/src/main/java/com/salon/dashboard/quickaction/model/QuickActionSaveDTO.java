package com.salon.dashboard.quickaction.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuickActionSaveDTO {
    @NotNull(message = "槽位不能为空")
    private Integer slot;
    @NotBlank(message = "显示名称不能为空")
    private String label;
    @NotBlank(message = "路由路径不能为空")
    private String path;
}
