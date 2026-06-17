package com.salon.menuconfig.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MenuConfigSaveWrapper {
    @NotEmpty(message = "配置列表不能为空")
    @Valid
    private List<MenuConfigSaveDTO> items;
}
