package com.salon.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductCategoryUpdateDTO {

    @NotBlank
    private String name;

    private Integer sort;
}
