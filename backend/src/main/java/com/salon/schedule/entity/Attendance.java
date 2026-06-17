package com.salon.schedule.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attendance")
public class Attendance extends BaseEntity {
    private Long employeeId;
    private Long scheduleId;
    private LocalDate date;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private Integer status;
    private String remark;
}
