package com.salon.groupbuy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import com.salon.groupbuy.service.GroupBuyOrderService;
import com.salon.groupbuy.vo.GroupBuyOrderPageVO;
import com.salon.groupbuy.vo.GroupBuyOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "拼团订单管理", description = "拼团团单查询与核销")
@RestController
@RequestMapping("/api/group-buy")
@RequiredArgsConstructor
public class GroupBuyOrderController {

    private final GroupBuyOrderService orderService;

    @GetMapping("/orders")
    @Operation(summary = "分页查询拼团团单")
    public Result<PageResult<GroupBuyOrderPageVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Page<GroupBuyOrderPageVO> p = orderService.page(page, size, templateId, status, startDate, endDate);
        return Result.success(PageResult.of(p));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "团单详情（含参团列表）")
    public Result<GroupBuyOrderVO> getDetail(@PathVariable Long id) {
        return Result.success(orderService.getDetail(id));
    }

    @PutMapping("/participants/{id}/redeem")
    @Operation(summary = "核销参团记录（2→3）")
    public Result<Void> redeem(@PathVariable Long id) {
        orderService.redeem(id);
        return Result.success();
    }
}
