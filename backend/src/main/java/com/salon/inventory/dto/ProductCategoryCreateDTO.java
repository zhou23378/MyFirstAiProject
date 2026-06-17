package com.salon.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductCategoryCreateDTO {

    @NotBlank
    private String name;

    private Integer sort;
}
