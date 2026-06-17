package com.salon.inventory.controller;

import com.salon.common.result.Result;
import com.salon.inventory.dto.SupplierCreateDTO;
import com.salon.inventory.dto.SupplierUpdateDTO;
import com.salon.inventory.entity.Supplier;
import com.salon.inventory.mapper.SupplierMapper;
import com.salon.inventory.vo.SupplierVO;
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

@Tag(name = "供应商管理", description = "供应商CRUD")
@RestController
@RequestMapping("/api/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierMapper supplierMapper;

    @GetMapping
    @Operation(summary = "获取全部供应商")
    public Result<List<SupplierVO>> list() {
        List<Supplier> entities = supplierMapper.selectList(null);
        List<SupplierVO> vos = entities.stream().map(SupplierVO::from).toList();
        return Result.success(vos);
    }

    @PostMapping
    @Operation(summary = "新增供应商")
    public Result<SupplierVO> create(@Valid @RequestBody SupplierCreateDTO dto) {
        Supplier entity = new Supplier();
        BeanUtils.copyProperties(dto, entity);
        supplierMapper.insert(entity);
        return Result.success(SupplierVO.from(entity));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改供应商")
    public Result<SupplierVO> update(@PathVariable Long id, @Valid @RequestBody SupplierUpdateDTO dto) {
        Supplier entity = new Supplier();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        supplierMapper.updateById(entity);
        Supplier updated = supplierMapper.selectById(id);
        return Result.success(SupplierVO.from(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除供应商")
    public Result<Void> delete(@PathVariable Long id) {
        supplierMapper.deleteById(id);
        return Result.success();
    }
}
