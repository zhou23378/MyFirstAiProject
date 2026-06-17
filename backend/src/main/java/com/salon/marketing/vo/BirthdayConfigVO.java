package com.salon.marketing.vo;

import com.salon.marketing.entity.BirthdayConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
@Schema(description = "生日营销配置视图")
public class BirthdayConfigVO {

    public static BirthdayConfigVO from(BirthdayConfig entity) {
        if (entity == null) {
            return null;
        }
        BirthdayConfigVO vo = new BirthdayConfigVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "0=停用 1=启用")
    private Integer enabled;

    @Schema(description = "发放的优惠券模板ID")
    private Long couponTemplateId;

    @Schema(description = "0=不发短信 1=发送短信")
    private Integer smsEnabled;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
