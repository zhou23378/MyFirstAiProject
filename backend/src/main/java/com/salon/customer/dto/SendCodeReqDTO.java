package com.salon.customer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendCodeReqDTO {

    @NotBlank
    private String phone;
}
