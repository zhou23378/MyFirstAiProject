package com.salon.commission.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salon.commission.dto.CommissionSettlementReqDTO;
import com.salon.commission.service.CommissionSettlementService;
import com.salon.commission.vo.CommissionSettlementPageVO;
import com.salon.commission.vo.CommissionSettlementVO;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;

@Tag(name = "提成结算", description = "员工提成结算管理")
@RestController
@RequestMapping("/api/commission/settlements")
@RequiredArgsConstructor
public class CommissionSettlementController {

    private final CommissionSettlementService commissionSettlementService;

    @GetMapping
    @Operation(summary = "分页查询结算记录")
    public Result<PageResult<CommissionSettlementPageVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String periodStart,
            @RequestParam(required = false) String periodEnd) {
        Page<CommissionSettlementPageVO> p = commissionSettlementService.page(page, size, employeeId, status, periodStart, periodEnd);
        return Result.success(PageResult.of(p));
    }

    @GetMapping("/{id}")
    @Operation(summary = "结算详情（含订单明细）")
    public Result<CommissionSettlementVO> getById(@PathVariable Long id) {
        return Result.success(commissionSettlementService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "生成提成结算")
    public Result<List<CommissionSettlementVO>> create(@Valid @RequestBody CommissionSettlementReqDTO dto) {
        return Result.success(commissionSettlementService.create(dto));
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认结算（0→1）")
    public Result<Void> confirm(@PathVariable Long id) {
        commissionSettlementService.confirm(id);
        return Result.success();
    }

    @PutMapping("/{id}/pay")
    @Operation(summary = "标记已付（1→2）")
    public Result<Void> pay(@PathVariable Long id) {
        commissionSettlementService.pay(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除结算（仅草稿）")
    public Result<Void> delete(@PathVariable Long id) {
        commissionSettlementService.delete(id);
        return Result.success();
    }
}
