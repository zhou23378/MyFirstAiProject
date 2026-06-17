package com.salon.schedule.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClockDTO {
    @NotNull private Long employeeId;
    @NotNull private Integer type;
}
