package com.salon.points.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import com.salon.points.service.PointsExchangeRecordService;
import com.salon.points.vo.PointsExchangeRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "积分兑换记录", description = "积分兑换记录查询与领取")
@RestController
@RequestMapping("/api/points/exchange-records")
@RequiredArgsConstructor
public class PointsExchangeRecordController {

    private final PointsExchangeRecordService exchangeRecordService;

    @GetMapping
    @Operation(summary = "分页查询兑换记录")
    public Result<PageResult<PointsExchangeRecordVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String memberPhone,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Page<PointsExchangeRecordVO> p = exchangeRecordService.page(page, size, memberPhone, status, startDate, endDate);
        return Result.success(PageResult.of(p));
    }

    @PutMapping("/{id}/claim")
    @Operation(summary = "标记已领取（0→1）")
    public Result<Void> claim(@PathVariable Long id) {
        exchangeRecordService.claim(id);
        return Result.success();
    }
}
