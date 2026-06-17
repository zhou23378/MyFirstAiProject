package com.salon.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SupplierCreateDTO {

    @NotBlank
    private String name;

    private String contactPerson;

    @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式错误")
    private String phone;

    private String address;

    private Integer status;
}
