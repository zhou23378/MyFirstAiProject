package com.salon.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceItemCreateDTO {

    @NotNull
    private Long categoryId;

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal price;

    @NotNull
    @Min(1)
    private Integer duration;

    @NotNull
    private Integer status;

    private Integer commissionType;

    private BigDecimal commissionValue;
}
