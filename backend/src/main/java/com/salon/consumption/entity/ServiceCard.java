package com.salon.consumption.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("service_card")
@Schema(description = "次卡实体")
public class ServiceCard {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private Long serviceItemId;
    private String name;
    private Integer totalCount;
    private Integer usedCount;
    private BigDecimal price;
    private Integer status;
    private LocalDate expireDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
