package com.salon.groupbuy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import com.salon.groupbuy.dto.GroupBuyTemplateReqDTO;
import com.salon.groupbuy.service.GroupBuyTemplateService;
import com.salon.groupbuy.vo.GroupBuyTemplatePageVO;
import com.salon.groupbuy.vo.GroupBuyTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "拼团模板管理", description = "拼团活动模板CRUD")
@RestController
@RequestMapping("/api/group-buy/templates")
@RequiredArgsConstructor
public class GroupBuyTemplateController {

    private final GroupBuyTemplateService templateService;

    @GetMapping
    @Operation(summary = "分页查询拼团模板")
    public Result<PageResult<GroupBuyTemplatePageVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        Page<GroupBuyTemplatePageVO> p = templateService.page(page, size, name, status);
        return Result.success(PageResult.of(p));
    }

    @GetMapping("/{id}")
    @Operation(summary = "拼团模板详情")
    public Result<GroupBuyTemplateVO> getById(@PathVariable Long id) {
        return Result.success(templateService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增拼团模板")
    public Result<GroupBuyTemplateVO> create(@Valid @RequestBody GroupBuyTemplateReqDTO dto) {
        return Result.success(templateService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑拼团模板")
    public Result<GroupBuyTemplateVO> update(@PathVariable Long id, @Valid @RequestBody GroupBuyTemplateReqDTO dto) {
        return Result.success(templateService.update(id, dto));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "启用/停用模板")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        templateService.updateStatus(id, status);
        return Result.success();
    }
}
