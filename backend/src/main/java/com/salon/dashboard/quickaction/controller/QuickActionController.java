package com.salon.dashboard.quickaction.controller;

import com.salon.common.result.Result;
import com.salon.dashboard.quickaction.model.QuickActionSaveWrapper;
import com.salon.dashboard.quickaction.model.QuickActionVO;
import com.salon.dashboard.quickaction.service.QuickActionService;
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

@Tag(name = "仪表盘快捷入口配置", description = "仪表盘快捷入口的获取和配置")
@RestController
@RequestMapping("/api/dashboard/quick-actions")
@RequiredArgsConstructor
public class QuickActionController {

    private final QuickActionService quickActionService;

    @GetMapping
    @Operation(summary = "获取快捷入口配置")
    public Result<List<QuickActionVO>> list() {
        return Result.success(quickActionService.list());
    }

    @PutMapping
    @Operation(summary = "保存快捷入口配置（全量替换）")
    public Result<Void> saveAll(@Valid @RequestBody QuickActionSaveWrapper wrapper) {
        quickActionService.saveAll(wrapper.getItems());
        return Result.success();
    }
}
