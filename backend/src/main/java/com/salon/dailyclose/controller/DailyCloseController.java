package com.salon.dailyclose.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import com.salon.dailyclose.dto.DailyCloseReqDTO;
import com.salon.dailyclose.entity.DailyClose;
import com.salon.dailyclose.service.DailyCloseService;
import com.salon.dailyclose.vo.DailyCloseSummaryVO;
import com.salon.dailyclose.vo.DailyCloseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "财务日结", description = "每日财务对账与日结管理")
@RestController
@RequestMapping("/api/daily-close")
@RequiredArgsConstructor
public class DailyCloseController {

    private final DailyCloseService dailyCloseService;

    @GetMapping("/today")
    @Operation(summary = "今日系统汇总")
    public Result<DailyCloseSummaryVO> today() {
        return Result.success(dailyCloseService.getTodaySummary());
    }

    @PostMapping("/save")
    @Operation(summary = "保存人工录入")
    public Result<DailyCloseVO> save(@Valid @RequestBody DailyCloseReqDTO dto) {
        return Result.success(DailyCloseVO.from(dailyCloseService.submitEntry(dto)));
    }

    @PostMapping("/{id}/lock")
    @Operation(summary = "锁定日结")
    public Result<DailyCloseVO> lock(@PathVariable Long id, @RequestParam(defaultValue = "admin") String lockedBy) {
        return Result.success(DailyCloseVO.from(dailyCloseService.lock(id, lockedBy)));
    }

    @GetMapping("/history")
    @Operation(summary = "历史日结列表")
    public Result<PageResult<DailyCloseVO>> history(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<DailyClose> p = dailyCloseService.page(
            new Page<>(page, pageSize),
            new LambdaQueryWrapper<DailyClose>().orderByDesc(DailyClose::getCloseDate));
        return Result.success(PageResult.of(p, DailyCloseVO::from));
    }

    @GetMapping("/{id}")
    @Operation(summary = "日结详情")
    public Result<DailyCloseVO> getById(@PathVariable Long id) {
        DailyClose entity = dailyCloseService.getById(id);
        if (entity == null) return Result.notFound("日结记录不存在");
        return Result.success(DailyCloseVO.from(entity));
    }
}
