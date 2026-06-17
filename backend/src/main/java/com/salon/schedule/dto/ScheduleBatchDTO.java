package com.salon.schedule.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleBatchDTO {
    @NotNull private Long employeeId;
    @NotNull private Long shiftId;
    @NotNull private LocalDate date;
}
