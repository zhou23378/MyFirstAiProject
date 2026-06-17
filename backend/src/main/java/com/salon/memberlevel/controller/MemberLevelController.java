package com.salon.memberlevel.controller;

import com.salon.common.result.Result;
import com.salon.memberlevel.dto.MemberLevelCreateDTO;
import com.salon.memberlevel.dto.MemberLevelUpdateDTO;
import com.salon.memberlevel.entity.MemberLevel;
import com.salon.memberlevel.service.MemberLevelService;
import com.salon.memberlevel.vo.MemberLevelVO;
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
 * 会员等级管理 Controller
 * <p>
 * 提供会员等级的增删改查 REST API
 * </p>
 */
@Tag(name = "会员等级管理", description = "会员等级的增删改查接口")
@RestController
@RequestMapping("/api/member-level")
@RequiredArgsConstructor
public class MemberLevelController {

    private final MemberLevelService memberLevelService;

    @Operation(summary = "查询所有等级列表")
    @GetMapping("/list")
    public Result<List<MemberLevelVO>> list() {
        List<MemberLevel> list = memberLevelService.list();
        return Result.success(list.stream().map(MemberLevelVO::from).toList());
    }

    @Operation(summary = "根据ID查询等级")
    @GetMapping("/{id}")
    public Result<MemberLevelVO> getById(@PathVariable Long id) {
        MemberLevel level = memberLevelService.getById(id);
        if (level == null) {
            return Result.notFound("等级不存在");
        }
        return Result.success(MemberLevelVO.from(level));
    }

    @Operation(summary = "新增等级")
    @PostMapping
    public Result<MemberLevelVO> create(@Valid @RequestBody MemberLevelCreateDTO dto) {
        MemberLevel entity = new MemberLevel();
        BeanUtils.copyProperties(dto, entity);
        memberLevelService.save(entity);
        return Result.success(MemberLevelVO.from(entity));
    }

    @Operation(summary = "修改等级")
    @PutMapping("/{id}")
    public Result<MemberLevelVO> update(@PathVariable Long id, @Valid @RequestBody MemberLevelUpdateDTO dto) {
        MemberLevel entity = new MemberLevel();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        memberLevelService.updateById(entity);
        MemberLevel updated = memberLevelService.getById(id);
        return Result.success(MemberLevelVO.from(updated));
    }

    @Operation(summary = "删除等级")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        memberLevelService.removeById(id);
        return Result.success();
    }
}
