package com.salon.coupon.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import com.salon.coupon.dto.CouponTemplateReqDTO;
import com.salon.coupon.dto.IssueCouponDTO;
import com.salon.coupon.dto.VerifyCouponDTO;
import com.salon.coupon.entity.CouponTemplate;
import com.salon.coupon.mapper.CouponTemplateMapper;
import com.salon.coupon.service.CouponService;
import com.salon.coupon.vo.CouponPageVO;
import com.salon.coupon.vo.CouponTemplateVO;
import com.salon.coupon.vo.CouponVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "优惠券管理", description = "优惠券模板CRUD、发券、核销")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CouponController {

    private final CouponTemplateMapper templateMapper;
    private final CouponService couponService;

    // ==================== 优惠券模板 ====================

    @GetMapping("/coupon-template")
    @Operation(summary = "优惠券模板列表")
    public Result<List<CouponTemplateVO>> listTemplates() {
        List<CouponTemplate> list = templateMapper.selectList(null);
        return Result.success(list.stream().map(CouponTemplateVO::from).collect(Collectors.toList()));
    }

    @GetMapping("/coupon-template/{id}")
    @Operation(summary = "查询优惠券模板详情")
    public Result<CouponTemplateVO> getTemplate(@PathVariable Long id) {
        CouponTemplate entity = templateMapper.selectById(id);
        if (entity == null) return Result.notFound("模板不存在");
        return Result.success(CouponTemplateVO.from(entity));
    }

    @PostMapping("/coupon-template")
    @Operation(summary = "新增优惠券模板")
    public Result<CouponTemplateVO> createTemplate(@Valid @RequestBody CouponTemplateReqDTO dto) {
        CouponTemplate entity = new CouponTemplate();
        BeanUtils.copyProperties(dto, entity);
        entity.setIssuedQty(0);
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        templateMapper.insert(entity);
        return Result.success(CouponTemplateVO.from(entity));
    }

    @PutMapping("/coupon-template/{id}")
    @Operation(summary = "修改优惠券模板")
    public Result<CouponTemplateVO> updateTemplate(@PathVariable Long id, @Valid @RequestBody CouponTemplateReqDTO dto) {
        CouponTemplate entity = templateMapper.selectById(id);
        if (entity == null) return Result.notFound("模板不存在");
        BeanUtils.copyProperties(dto, entity, "id", "createTime", "updateTime", "issuedQty");
        templateMapper.updateById(entity);
        return Result.success(CouponTemplateVO.from(entity));
    }

    @DeleteMapping("/coupon-template/{id}")
    @Operation(summary = "删除优惠券模板")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        templateMapper.deleteById(id);
        return Result.success();
    }

    // ==================== 优惠券 ====================

    @GetMapping("/coupon/page")
    @Operation(summary = "分页查询优惠券列表（含模板名/会员名）")
    public Result<PageResult<CouponPageVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Integer status) {
        Page<CouponPageVO> p = couponService.pageWithNames(page, pageSize, memberId, status);
        return Result.success(PageResult.of(p));
    }

    @PostMapping("/coupon/issue")
    @Operation(summary = "发券")
    public Result<CouponVO> issue(@Valid @RequestBody IssueCouponDTO dto) {
        return Result.success(CouponVO.from(couponService.issue(dto.getTemplateId(), dto.getMemberId())));
    }

    @PostMapping("/coupon/verify")
    @Operation(summary = "核销优惠券，返回优惠金额")
    public Result<BigDecimal> verify(@Valid @RequestBody VerifyCouponDTO dto) {
        return Result.success(couponService.verify(dto.getCode(), dto.getOrderId(), dto.getOrderAmount()));
    }
}
