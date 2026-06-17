package com.salon.appointment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentStatusDTO {

    @NotNull
    private Integer status;
}
