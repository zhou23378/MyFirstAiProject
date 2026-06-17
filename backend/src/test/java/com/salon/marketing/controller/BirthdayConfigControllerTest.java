package com.salon.marketing.controller;

import com.salon.common.result.Result;
import com.salon.marketing.dto.BirthdayConfigReqDTO;
import com.salon.marketing.entity.BirthdayConfig;
import com.salon.marketing.mapper.BirthdayConfigMapper;
import com.salon.marketing.vo.BirthdayConfigVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BirthdayConfigControllerTest {

    private BirthdayConfigMapper birthdayConfigMapper;
    private BirthdayConfigController controller;

    @BeforeEach
    void setUp() {
        birthdayConfigMapper = mock(BirthdayConfigMapper.class);
        controller = new BirthdayConfigController(birthdayConfigMapper);
    }

    @Test
    void get_shouldReturnDefaultVoWhenConfigMissing() {
        when(birthdayConfigMapper.selectById(1L)).thenReturn(null);

        Result<BirthdayConfigVO> result = controller.get();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getId()).isEqualTo(1L);
        assertThat(result.getData().getEnabled()).isEqualTo(1);
        assertThat(result.getData().getSmsEnabled()).isEqualTo(1);
        assertThat(result.getData().getCouponTemplateId()).isNull();
    }

    @Test
    void update_shouldInsertAndReturnVoWhenConfigMissing() {
        when(birthdayConfigMapper.selectById(1L)).thenReturn(null);
        when(birthdayConfigMapper.insert(org.mockito.ArgumentMatchers.any(BirthdayConfig.class)))
                .thenAnswer(invocation -> {
                    BirthdayConfig entity = invocation.getArgument(0);
                    entity.setUpdateTime(null);
                    return 1;
                });

        BirthdayConfigReqDTO req = new BirthdayConfigReqDTO();
        req.setEnabled(0);
        req.setCouponTemplateId(3L);
        req.setSmsEnabled(0);

        Result<BirthdayConfigVO> result = controller.update(req);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getEnabled()).isEqualTo(0);
        assertThat(result.getData().getCouponTemplateId()).isEqualTo(3L);
        assertThat(result.getData().getSmsEnabled()).isEqualTo(0);
        verify(birthdayConfigMapper).insert(org.mockito.ArgumentMatchers.any(BirthdayConfig.class));
    }

    @Test
    void update_shouldPreserveTemplateWhenFieldMissing() {
        BirthdayConfig existing = new BirthdayConfig();
        existing.setId(1L);
        existing.setEnabled(1);
        existing.setCouponTemplateId(3L);
        existing.setSmsEnabled(1);
        when(birthdayConfigMapper.selectById(1L)).thenReturn(existing);

        BirthdayConfigReqDTO req = new BirthdayConfigReqDTO();

        Result<BirthdayConfigVO> result = controller.update(req);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getEnabled()).isEqualTo(1);
        assertThat(result.getData().getCouponTemplateId()).isEqualTo(3L);
        assertThat(result.getData().getSmsEnabled()).isEqualTo(1);
        verify(birthdayConfigMapper).updateById(existing);
    }

    @Test
    void update_shouldAllowClearingTemplateWhenNullProvided() {
        BirthdayConfig existing = new BirthdayConfig();
        existing.setId(1L);
        existing.setEnabled(1);
        existing.setCouponTemplateId(3L);
        existing.setSmsEnabled(1);
        when(birthdayConfigMapper.selectById(1L)).thenReturn(existing);

        BirthdayConfigReqDTO req = new BirthdayConfigReqDTO();
        req.setCouponTemplateId(null);

        Result<BirthdayConfigVO> result = controller.update(req);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getCouponTemplateId()).isNull();
        verify(birthdayConfigMapper).updateById(existing);
    }
}
