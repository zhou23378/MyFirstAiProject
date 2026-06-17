package com.salon.inventory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import com.salon.common.util.JwtUtil;
import com.salon.inventory.dto.ReturnOrderReqDTO;
import com.salon.inventory.service.ReturnOrderService;
import com.salon.inventory.vo.ReturnOrderPageVO;
import com.salon.inventory.vo.ReturnOrderVO;
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

@Tag(name = "退货管理", description = "退货订单管理接口")
@RestController
@RequestMapping("/api/return-order")
@RequiredArgsConstructor
public class ReturnOrderController {

    private final ReturnOrderService returnOrderService;
    private final JwtUtil jwtUtil;

    @GetMapping("/page")
    @Operation(summary = "分页查询退货订单")
    public Result<PageResult<ReturnOrderPageVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long supplierId) {
        Page<ReturnOrderPageVO> p = returnOrderService.page(page, pageSize, status, supplierId);
        return Result.success(PageResult.of(p));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询退货订单详情")
    public Result<ReturnOrderVO> getById(@PathVariable Long id) {
        return Result.success(returnOrderService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "创建退货订单")
    public Result<ReturnOrderVO> create(@Valid @RequestBody ReturnOrderReqDTO dto, HttpServletRequest request) {
        String applicant = extractOperator(request);
        return Result.success(returnOrderService.create(dto, applicant));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑退货订单（仅草稿状态）")
    public Result<ReturnOrderVO> update(@PathVariable Long id, @Valid @RequestBody ReturnOrderReqDTO dto) {
        return Result.success(returnOrderService.update(id, dto));
    }

    @PutMapping("/{id}/submit")
    @Operation(summary = "提交审核（0→1）")
    public Result<Void> submit(@PathVariable Long id) {
        returnOrderService.submit(id);
        return Result.success();
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "审核通过（1→2）")
    public Result<Void> approve(@PathVariable Long id, HttpServletRequest request) {
        String approver = extractOperator(request);
        returnOrderService.approve(id, approver);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "驳回（1→4）")
    public Result<Void> reject(@PathVariable Long id, HttpServletRequest request) {
        String approver = extractOperator(request);
        returnOrderService.reject(id, approver);
        return Result.success();
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "完成退货（2→3）")
    public Result<Void> complete(@PathVariable Long id) {
        returnOrderService.complete(id);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单（仅草稿状态）")
    public Result<Void> cancel(@PathVariable Long id) {
        returnOrderService.cancel(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除订单（仅草稿状态）")
    public Result<Void> delete(@PathVariable Long id) {
        returnOrderService.delete(id);
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
