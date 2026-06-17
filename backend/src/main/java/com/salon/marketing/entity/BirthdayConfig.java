package com.salon.marketing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("birthday_config")
public class BirthdayConfig {
    private Long id;
    private Integer enabled;
    private Long couponTemplateId;
    private Integer smsEnabled;
    private LocalDateTime updateTime;
}
