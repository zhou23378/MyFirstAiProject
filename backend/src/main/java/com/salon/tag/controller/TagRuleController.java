package com.salon.tag.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salon.common.result.Result;
import com.salon.tag.dto.TagRuleReqDTO;
import com.salon.tag.entity.TagRule;
import com.salon.tag.service.TagRuleService;
import com.salon.tag.vo.TagRuleVO;
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
import java.util.stream.Collectors;

@Tag(name = "标签规则", description = "自动标签规则管理")
@RestController
@RequestMapping("/api/tag-rules")
@RequiredArgsConstructor
public class TagRuleController {

    private final TagRuleService tagRuleService;

    @GetMapping
    @Operation(summary = "标签规则列表")
    public Result<List<TagRuleVO>> list() {
        List<TagRule> rules = tagRuleService.list(
            new LambdaQueryWrapper<TagRule>().orderByDesc(TagRule::getCreateTime));
        return Result.success(rules.stream().map(TagRuleVO::from).collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "新增标签规则")
    public Result<TagRuleVO> create(@Valid @RequestBody TagRuleReqDTO dto) {
        TagRule rule = new TagRule();
        BeanUtils.copyProperties(dto, rule);
        tagRuleService.save(rule);
        return Result.success(TagRuleVO.from(rule));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改标签规则")
    public Result<TagRuleVO> update(@PathVariable Long id, @Valid @RequestBody TagRuleReqDTO dto) {
        TagRule rule = tagRuleService.getById(id);
        if (rule == null) {
            return Result.notFound("标签规则不存在");
        }
        BeanUtils.copyProperties(dto, rule);
        tagRuleService.updateById(rule);
        return Result.success(TagRuleVO.from(rule));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签规则")
    public Result<Void> delete(@PathVariable Long id) {
        tagRuleService.removeById(id);
        return Result.success();
    }

    @PostMapping("/{id}/toggle")
    @Operation(summary = "启用/禁用规则")
    public Result<Void> toggle(@PathVariable Long id) {
        TagRule rule = tagRuleService.getById(id);
        if (rule != null) {
            rule.setEnabled(Integer.valueOf(1).equals(rule.getEnabled()) ? 0 : 1);
            tagRuleService.updateById(rule);
        }
        return Result.success();
    }
}
