package com.salon.timer.dto;

import lombok.Data;

@Data
public class TimerCompleteDTO {

    /** 实际耗时（分钟），可选 */
    private Integer actualDuration;
}
