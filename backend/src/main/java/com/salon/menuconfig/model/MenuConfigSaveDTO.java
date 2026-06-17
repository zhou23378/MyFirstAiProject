package com.salon.menuconfig.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuConfigSaveDTO {
    @NotBlank(message = "菜单路径不能为空")
    private String menuIndex;
    @NotBlank(message = "分组名称不能为空")
    private String groupName;
}
