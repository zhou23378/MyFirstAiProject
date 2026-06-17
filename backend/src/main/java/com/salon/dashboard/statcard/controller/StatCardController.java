package com.salon.dashboard.statcard.controller;

import com.salon.common.result.Result;
import com.salon.dashboard.statcard.model.StatCardSaveWrapper;
import com.salon.dashboard.statcard.model.StatCardVO;
import com.salon.dashboard.statcard.service.StatCardService;
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

@Tag(name = "仪表盘统计卡片配置", description = "仪表盘统计卡片的获取和配置")
@RestController
@RequestMapping("/api/dashboard/stat-cards")
@RequiredArgsConstructor
public class StatCardController {

    private final StatCardService statCardService;

    @GetMapping
    @Operation(summary = "获取统计卡片配置")
    public Result<List<StatCardVO>> list() {
        return Result.success(statCardService.list());
    }

    @PutMapping
    @Operation(summary = "保存统计卡片配置（全量替换）")
    public Result<Void> saveAll(@Valid @RequestBody StatCardSaveWrapper wrapper) {
        statCardService.saveAll(wrapper.getItems());
        return Result.success();
    }
}
