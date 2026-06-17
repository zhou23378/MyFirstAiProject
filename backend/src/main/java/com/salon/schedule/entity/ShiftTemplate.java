package com.salon.schedule.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shift_template")
public class ShiftTemplate extends BaseEntity {
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String color;
    private Integer sort;
}
