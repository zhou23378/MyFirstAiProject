package com.salon.inventory.controller;

import com.salon.common.result.Result;
import com.salon.inventory.dto.ProductCategoryCreateDTO;
import com.salon.inventory.dto.ProductCategoryUpdateDTO;
import com.salon.inventory.entity.ProductCategory;
import com.salon.inventory.mapper.ProductCategoryMapper;
import com.salon.inventory.vo.ProductCategoryVO;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "商品分类", description = "商品分类CRUD")
@RestController
@RequestMapping("/api/product-category")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryMapper productCategoryMapper;

    @GetMapping
    @Operation(summary = "获取全部分类")
    public Result<List<ProductCategoryVO>> list() {
        List<ProductCategory> entities = productCategoryMapper.selectList(null);
        List<ProductCategoryVO> vos = entities.stream().map(ProductCategoryVO::from).toList();
        return Result.success(vos);
    }

    @PostMapping
    @Operation(summary = "新增分类")
    public Result<ProductCategoryVO> create(@Valid @RequestBody ProductCategoryCreateDTO dto) {
        ProductCategory entity = new ProductCategory();
        BeanUtils.copyProperties(dto, entity);
        productCategoryMapper.insert(entity);
        return Result.success(ProductCategoryVO.from(entity));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改分类")
    public Result<ProductCategoryVO> update(@PathVariable Long id, @Valid @RequestBody ProductCategoryUpdateDTO dto) {
        ProductCategory entity = new ProductCategory();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        productCategoryMapper.updateById(entity);
        ProductCategory updated = productCategoryMapper.selectById(id);
        return Result.success(ProductCategoryVO.from(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public Result<Void> delete(@PathVariable Long id) {
        productCategoryMapper.deleteById(id);
        return Result.success();
    }
}
