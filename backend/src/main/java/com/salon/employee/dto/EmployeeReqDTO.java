package com.salon.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmployeeReqDTO {

    @NotBlank(message = "员工姓名不能为空")
    private String name;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String position;

    @PositiveOrZero(message = "薪资不能为负数")
    private BigDecimal salary;

    private Integer status;
}
