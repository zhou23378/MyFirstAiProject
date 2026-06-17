package com.salon.marketing.controller;

import com.salon.common.result.Result;
import com.salon.marketing.dto.BirthdayConfigReqDTO;
import com.salon.marketing.entity.BirthdayConfig;
import com.salon.marketing.mapper.BirthdayConfigMapper;
import com.salon.marketing.vo.BirthdayConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "生日营销配置", description = "生日礼券发放规则配置")
@RestController
@RequestMapping("/api/birthday-config")
@RequiredArgsConstructor
public class BirthdayConfigController {

    private static final Long CONFIG_ID = 1L;

    private final BirthdayConfigMapper birthdayConfigMapper;

    @GetMapping
    @Operation(summary = "获取生日营销配置")
    public Result<BirthdayConfigVO> get() {
        BirthdayConfig config = birthdayConfigMapper.selectById(CONFIG_ID);
        if (config == null) {
            config = defaultConfig();
        }
        return Result.success(BirthdayConfigVO.from(config));
    }

    @PutMapping
    @Operation(summary = "更新生日营销配置")
    public Result<BirthdayConfigVO> update(@Valid @RequestBody BirthdayConfigReqDTO req) {
        BirthdayConfig config = birthdayConfigMapper.selectById(CONFIG_ID);
        boolean exists = config != null;
        if (config == null) {
            config = defaultConfig();
        }
        if (req.getEnabled() != null) {
            config.setEnabled(req.getEnabled());
        }
        if (req.hasCouponTemplateId()) {
            config.setCouponTemplateId(req.getCouponTemplateId());
        }
        if (req.getSmsEnabled() != null) {
            config.setSmsEnabled(req.getSmsEnabled());
        }
        if (exists) {
            birthdayConfigMapper.updateById(config);
        } else {
            birthdayConfigMapper.insert(config);
        }
        return Result.success(BirthdayConfigVO.from(config));
    }

    private BirthdayConfig defaultConfig() {
        BirthdayConfig config = new BirthdayConfig();
        config.setId(CONFIG_ID);
        config.setEnabled(1);
        config.setSmsEnabled(1);
        return config;
    }
}
