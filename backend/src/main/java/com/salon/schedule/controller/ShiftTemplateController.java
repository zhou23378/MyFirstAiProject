package com.salon.schedule.controller;

import com.salon.common.result.Result;
import com.salon.schedule.dto.ShiftTemplateCreateDTO;
import com.salon.schedule.dto.ShiftTemplateUpdateDTO;
import com.salon.schedule.entity.ShiftTemplate;
import com.salon.schedule.service.ShiftTemplateService;
import com.salon.schedule.vo.ShiftTemplateVO;
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

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "班次模板", description = "班次模板 CRUD")
@RestController
@RequestMapping("/api/shift-template")
@RequiredArgsConstructor
public class ShiftTemplateController {

    private final ShiftTemplateService shiftTemplateService;

    @GetMapping
    @Operation(summary = "获取全部班次模板")
    public Result<List<ShiftTemplateVO>> list() {
        return Result.success(shiftTemplateService.list().stream()
                .map(ShiftTemplateVO::from).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取单个班次模板")
    public Result<ShiftTemplateVO> get(@PathVariable Long id) {
        ShiftTemplate entity = shiftTemplateService.getById(id);
        return entity != null ? Result.success(ShiftTemplateVO.from(entity))
                : Result.notFound("班次模板不存在");
    }

    @PostMapping
    @Operation(summary = "新增班次模板")
    public Result<ShiftTemplateVO> create(@Valid @RequestBody ShiftTemplateCreateDTO dto) {
        ShiftTemplate entity = new ShiftTemplate();
        entity.setName(dto.getName());
        entity.setStartTime(LocalTime.parse(dto.getStartTime()));
        entity.setEndTime(LocalTime.parse(dto.getEndTime()));
        shiftTemplateService.save(entity);
        return Result.success(ShiftTemplateVO.from(entity));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改班次模板")
    public Result<ShiftTemplateVO> update(@PathVariable Long id, @Valid @RequestBody ShiftTemplateUpdateDTO dto) {
        ShiftTemplate entity = shiftTemplateService.getById(id);
        if (entity == null) {
            return Result.notFound("班次模板不存在");
        }
        entity.setName(dto.getName());
        entity.setStartTime(LocalTime.parse(dto.getStartTime()));
        entity.setEndTime(LocalTime.parse(dto.getEndTime()));
        shiftTemplateService.updateById(entity);
        return Result.success(ShiftTemplateVO.from(entity));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除班次模板")
    public Result<Void> delete(@PathVariable Long id) {
        shiftTemplateService.removeById(id);
        return Result.success();
    }
}
