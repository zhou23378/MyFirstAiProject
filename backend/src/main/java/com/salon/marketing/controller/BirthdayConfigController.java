package com.salon.marketing.controller;

import com.salon.common.result.Result;
import com.salon.marketing.entity.BirthdayConfig;
import com.salon.marketing.mapper.BirthdayConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "生日营销配置", description = "生日礼券发放规则配置")
@RestController
@RequestMapping("/api/birthday-config")
@RequiredArgsConstructor
public class BirthdayConfigController {

    private final BirthdayConfigMapper birthdayConfigMapper;

    @GetMapping
    @Operation(summary = "获取生日营销配置")
    public Result<BirthdayConfig> get() {
        BirthdayConfig config = birthdayConfigMapper.selectById(1);
        if (config == null) {
            config = new BirthdayConfig();
            config.setId(1L);
            config.setEnabled(1);
            config.setSmsEnabled(1);
        }
        return Result.success(config);
    }

    @PutMapping
    @Operation(summary = "更新生日营销配置")
    public Result<Void> update(@RequestBody Map<String, Object> body) {
        BirthdayConfig config = birthdayConfigMapper.selectById(1);
        if (config == null) {
            config = new BirthdayConfig();
            config.setId(1L);
        }
        if (body.containsKey("enabled")) config.setEnabled((Integer) body.get("enabled"));
        if (body.containsKey("couponTemplateId")) config.setCouponTemplateId(body.get("couponTemplateId") != null
                ? Long.valueOf(body.get("couponTemplateId").toString()) : null);
        if (body.containsKey("smsEnabled")) config.setSmsEnabled((Integer) body.get("smsEnabled"));
        if (birthdayConfigMapper.selectById(1) != null) {
            birthdayConfigMapper.updateById(config);
        } else {
            birthdayConfigMapper.insert(config);
        }
        return Result.success();
    }
}
