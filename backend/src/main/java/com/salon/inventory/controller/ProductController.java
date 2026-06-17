package com.salon.inventory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import com.salon.inventory.dto.ProductReqDTO;
import com.salon.inventory.entity.Product;
import com.salon.inventory.service.ProductService;
import com.salon.inventory.vo.ProductPageVO;
import com.salon.inventory.vo.ProductVO;
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

@Tag(name = "商品管理", description = "商品的增删改查接口")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/page")
    @Operation(summary = "分页查询商品列表（含分类名/供应商名）")
    public Result<PageResult<ProductPageVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        Page<ProductPageVO> p = productService.pageWithNames(page, pageSize, categoryId, keyword);
        return Result.success(PageResult.of(p));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "库存预警商品列表")
    public Result<PageResult<ProductPageVO>> lowStock(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<ProductPageVO> p = productService.lowStock(page, pageSize);
        return Result.success(PageResult.of(p));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询商品详情")
    public Result<ProductVO> getById(@PathVariable Long id) {
        Product entity = productService.getById(id);
        if (entity == null) return Result.notFound("商品不存在");
        return Result.success(ProductVO.from(entity));
    }

    @PostMapping
    @Operation(summary = "新增商品")
    public Result<ProductVO> create(@Valid @RequestBody ProductReqDTO dto) {
        Product entity = new Product();
        BeanUtils.copyProperties(dto, entity);
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        entity.setStockQty(dto.getStockQty() != null ? dto.getStockQty() : 0);
        entity.setAlertQty(dto.getAlertQty() != null ? dto.getAlertQty() : 10);
        productService.save(entity);
        return Result.success(ProductVO.from(entity));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改商品")
    public Result<ProductVO> update(@PathVariable Long id, @Valid @RequestBody ProductReqDTO dto) {
        Product entity = productService.getById(id);
        if (entity == null) return Result.notFound("商品不存在");
        BeanUtils.copyProperties(dto, entity, "id", "createTime", "updateTime");
        productService.updateById(entity);
        return Result.success(ProductVO.from(entity));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品")
    public Result<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success();
    }
}
