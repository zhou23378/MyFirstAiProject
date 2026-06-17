package com.salon.points.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import com.salon.points.dto.PointsProductReqDTO;
import com.salon.points.service.PointsProductService;
import com.salon.points.vo.PointsProductPageVO;
import com.salon.points.vo.PointsProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "积分商品管理", description = "积分商城商品CRUD")
@RestController
@RequestMapping("/api/points/products")
@RequiredArgsConstructor
public class PointsProductController {

    private final PointsProductService pointsProductService;

    @GetMapping
    @Operation(summary = "分页查询积分商品")
    public Result<PageResult<PointsProductPageVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        Page<PointsProductPageVO> p = pointsProductService.page(page, size, name, status);
        return Result.success(PageResult.of(p));
    }

    @GetMapping("/{id}")
    @Operation(summary = "积分商品详情")
    public Result<PointsProductVO> getById(@PathVariable Long id) {
        return Result.success(pointsProductService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增积分商品")
    public Result<PointsProductVO> create(@Valid @RequestBody PointsProductReqDTO dto) {
        return Result.success(pointsProductService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑积分商品")
    public Result<PointsProductVO> update(@PathVariable Long id, @Valid @RequestBody PointsProductReqDTO dto) {
        return Result.success(pointsProductService.update(id, dto));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "上架/下架商品")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        pointsProductService.updateStatus(id, status);
        return Result.success();
    }
}
