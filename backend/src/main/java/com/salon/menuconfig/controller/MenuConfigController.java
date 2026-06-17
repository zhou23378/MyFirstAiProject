package com.salon.menuconfig.controller;

import com.salon.common.result.Result;
import com.salon.menuconfig.model.MenuConfigSaveWrapper;
import com.salon.menuconfig.model.MenuConfigVO;
import com.salon.menuconfig.service.MenuConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "菜单配置", description = "菜单分组配置管理")
@RestController
@RequestMapping("/api/menu-config")
@RequiredArgsConstructor
public class MenuConfigController {

    private final MenuConfigService menuConfigService;

    @GetMapping
    @Operation(summary = "获取菜单分组配置")
    public Result<List<MenuConfigVO>> list() {
        return Result.success(menuConfigService.list());
    }

    @PutMapping
    @Operation(summary = "保存菜单分组配置（全量替换）")
    public Result<Void> saveAll(@Valid @RequestBody MenuConfigSaveWrapper wrapper) {
        menuConfigService.saveAll(wrapper.getItems());
        return Result.success();
    }
}
