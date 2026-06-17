package com.salon.consumption.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import com.salon.consumption.dto.ServiceCardReqDTO;
import com.salon.consumption.entity.ServiceCard;
import com.salon.consumption.service.ServiceCardService;
import com.salon.consumption.vo.ServiceCardVO;
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

import java.util.stream.Collectors;

@Tag(name = "次卡管理", description = "次卡购买、查询、扣次")
@RestController
@RequestMapping("/api/service-card")
@RequiredArgsConstructor
public class ServiceCardController {

    private final ServiceCardService serviceCardService;

    @GetMapping("/page")
    @Operation(summary = "分页查询次卡")
    public Result<PageResult<ServiceCardVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long memberId) {
        LambdaQueryWrapper<ServiceCard> q = new LambdaQueryWrapper<ServiceCard>()
                .orderByDesc(ServiceCard::getCreateTime);
        if (memberId != null) {
            q.eq(ServiceCard::getMemberId, memberId);
        }
        Page<ServiceCard> p = serviceCardService.page(new Page<>(page, pageSize), q);
        Page<ServiceCardVO> voPage = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        voPage.setRecords(p.getRecords().stream().map(ServiceCardVO::from).collect(Collectors.toList()));
        return Result.success(PageResult.of(voPage));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询次卡详情")
    public Result<ServiceCardVO> get(@PathVariable Long id) {
        return Result.success(ServiceCardVO.from(serviceCardService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "购买次卡（从会员余额扣款）")
    public Result<ServiceCardVO> purchase(@Valid @RequestBody ServiceCardReqDTO dto) {
        ServiceCard card = new ServiceCard();
        card.setMemberId(dto.getMemberId());
        card.setServiceItemId(dto.getServiceItemId());
        card.setTotalCount(dto.getTotalCount());
        card.setPrice(dto.getPrice());
        return Result.success(ServiceCardVO.from(serviceCardService.purchase(card)));
    }

    @PutMapping("/{id}/deduct")
    @Operation(summary = "扣次（使用一次）")
    public Result<ServiceCardVO> deduct(@PathVariable Long id) {
        return Result.success(ServiceCardVO.from(serviceCardService.deduct(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除次卡")
    public Result<Void> delete(@PathVariable Long id) {
        serviceCardService.removeById(id);
        return Result.success();
    }
}
