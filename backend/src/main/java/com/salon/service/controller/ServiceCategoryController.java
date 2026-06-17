package com.salon.service.controller;

import com.salon.common.result.Result;
import com.salon.service.dto.ServiceCategoryCreateDTO;
import com.salon.service.dto.ServiceCategoryUpdateDTO;
import com.salon.service.entity.ServiceCategory;
import com.salon.service.service.ServiceCategoryService;
import com.salon.service.vo.ServiceCategoryVO;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 服务分类 Controller
 * <p>
 * 提供服务分类的增删改查 REST API
 * </p>
 */
@Tag(name = "服务分类管理", description = "服务分类的增删改查接口")
@RestController
@RequestMapping("/api/service-category")
@RequiredArgsConstructor
public class ServiceCategoryController {

    private final ServiceCategoryService serviceCategoryService;

    /**
     * 查询所有分类列表
     *
     * @return 分类列表
     */
    @Operation(summary = "查询所有分类", description = "按排序号升序返回所有分类")
    @GetMapping
    public Result<List<ServiceCategoryVO>> list() {
        List<ServiceCategory> list = serviceCategoryService.findAllOrderBySort();
        return Result.success(list.stream().map(ServiceCategoryVO::from).toList());
    }

    /**
     * 新增分类
     *
     * @param category 分类信息
     * @return 新增后的分类
     */
    @Operation(summary = "新增分类")
    @PostMapping
    public Result<ServiceCategoryVO> create(@Valid @RequestBody ServiceCategoryCreateDTO dto) {
        ServiceCategory category = new ServiceCategory();
        category.setName(dto.getName());
        category.setSort(dto.getSortOrder());
        serviceCategoryService.save(category);
        return Result.success(ServiceCategoryVO.from(category));
    }

    /**
     * 修改分类
     *
     * @param id       分类ID
     * @param category 分类信息
     * @return 修改后的分类
     */
    @Operation(summary = "修改分类")
    @PutMapping("/{id}")
    public Result<ServiceCategoryVO> update(@PathVariable Long id, @Valid @RequestBody ServiceCategoryUpdateDTO dto) {
        ServiceCategory category = new ServiceCategory();
        category.setId(id);
        category.setName(dto.getName());
        category.setSort(dto.getSortOrder());
        serviceCategoryService.updateById(category);
        return Result.success(ServiceCategoryVO.from(category));
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 操作结果
     */
    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        serviceCategoryService.removeById(id);
        return Result.success();
    }
}
