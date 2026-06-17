package com.salon.service.controller;

import com.salon.common.result.Result;
import com.salon.service.dto.ServiceItemCreateDTO;
import com.salon.service.dto.ServiceItemUpdateDTO;
import com.salon.service.entity.ServiceItem;
import com.salon.service.service.ServiceItemService;
import com.salon.service.vo.ServiceItemVO;
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

/**
 * 服务项目 Controller
 * <p>
 * 提供服务项目的增删改查 REST API
 * </p>
 */
@Tag(name = "服务项目管理", description = "服务项目的增删改查接口")
@RestController
@RequestMapping("/api/service-item")
@RequiredArgsConstructor
public class ServiceItemController {

    private final ServiceItemService serviceItemService;

    /**
     * 查询所有服务项目
     *
     * @return 所有项目列表
     */
    @Operation(summary = "查询所有项目", description = "返回所有服务项目列表")
    @GetMapping
    public Result<List<ServiceItemVO>> listAll() {
        List<ServiceItem> list = serviceItemService.list();
        return Result.success(list.stream().map(ServiceItemVO::from).toList());
    }

    /**
     * 根据分类ID查询项目列表
     *
     * @param categoryId 分类ID
     * @return 项目列表
     */
    @Operation(summary = "根据分类查询项目", description = "按分类ID查询该分类下的所有服务项目")
    @GetMapping("/category/{categoryId}")
    public Result<List<ServiceItemVO>> listByCategory(@PathVariable Long categoryId) {
        List<ServiceItem> list = serviceItemService.findByCategoryId(categoryId);
        return Result.success(list.stream().map(ServiceItemVO::from).toList());
    }

    /**
     * 新增项目
     *
     * @param item 项目信息
     * @return 新增后的项目
     */
    @Operation(summary = "新增项目")
    @PostMapping
    public Result<ServiceItemVO> create(@Valid @RequestBody ServiceItemCreateDTO dto) {
        ServiceItem item = new ServiceItem();
        BeanUtils.copyProperties(dto, item);
        serviceItemService.save(item);
        return Result.success(ServiceItemVO.from(item));
    }

    /**
     * 修改项目
     *
     * @param id   项目ID
     * @param item 项目信息
     * @return 修改后的项目
     */
    @Operation(summary = "修改项目")
    @PutMapping("/{id}")
    public Result<ServiceItemVO> update(@PathVariable Long id, @Valid @RequestBody ServiceItemUpdateDTO dto) {
        ServiceItem item = new ServiceItem();
        BeanUtils.copyProperties(dto, item);
        item.setId(id);
        serviceItemService.updateById(item);
        return Result.success(ServiceItemVO.from(item));
    }

    /**
     * 删除项目
     *
     * @param id 项目ID
     * @return 操作结果
     */
    @Operation(summary = "删除项目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        serviceItemService.removeById(id);
        return Result.success();
    }
}
