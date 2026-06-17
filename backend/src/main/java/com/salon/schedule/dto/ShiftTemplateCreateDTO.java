package com.salon.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShiftTemplateCreateDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String startTime;

    @NotBlank
    private String endTime;
}
