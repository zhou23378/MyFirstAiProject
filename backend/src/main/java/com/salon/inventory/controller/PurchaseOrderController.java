package com.salon.inventory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import com.salon.common.util.JwtUtil;
import com.salon.inventory.dto.PurchaseOrderReqDTO;
import com.salon.inventory.service.PurchaseOrderService;
import com.salon.inventory.vo.PurchaseOrderPageVO;
import com.salon.inventory.vo.PurchaseOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "采购订单", description = "采购订单管理接口")
@RestController
@RequestMapping("/api/purchase-order")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final JwtUtil jwtUtil;

    @GetMapping("/page")
    @Operation(summary = "分页查询采购订单")
    public Result<PageResult<PurchaseOrderPageVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long supplierId) {
        Page<PurchaseOrderPageVO> p = purchaseOrderService.page(page, pageSize, status, supplierId);
        return Result.success(PageResult.of(p));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询采购订单详情")
    public Result<PurchaseOrderVO> getById(@PathVariable Long id) {
        return Result.success(purchaseOrderService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "创建采购订单")
    public Result<PurchaseOrderVO> create(@Valid @RequestBody PurchaseOrderReqDTO dto, HttpServletRequest request) {
        String applicant = extractOperator(request);
        return Result.success(purchaseOrderService.create(dto, applicant));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑采购订单（仅草稿状态）")
    public Result<PurchaseOrderVO> update(@PathVariable Long id, @Valid @RequestBody PurchaseOrderReqDTO dto) {
        return Result.success(purchaseOrderService.update(id, dto));
    }

    @PutMapping("/{id}/submit")
    @Operation(summary = "提交审核（0→1）")
    public Result<Void> submit(@PathVariable Long id) {
        purchaseOrderService.submit(id);
        return Result.success();
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "审核通过（1→2）")
    public Result<Void> approve(@PathVariable Long id, HttpServletRequest request) {
        String approver = extractOperator(request);
        purchaseOrderService.approve(id, approver);
        return Result.success();
    }

    @PutMapping("/{id}/receive")
    @Operation(summary = "确认收货（2→3）")
    public Result<Void> receive(@PathVariable Long id) {
        purchaseOrderService.receive(id);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单（0→4 或 1→4）")
    public Result<Void> cancel(@PathVariable Long id) {
        purchaseOrderService.cancel(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除订单（仅草稿状态）")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseOrderService.delete(id);
        return Result.success();
    }

    private String extractOperator(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return "unknown";
        }
        String token = auth.substring(7);
        return jwtUtil.getUsername(token);
    }
}
