package com.salon.employee.vo;

import com.salon.employee.entity.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "员工视图")
public class EmployeeVO {

    public static EmployeeVO from(Employee entity) {
        if (entity == null) return null;
        EmployeeVO vo = new EmployeeVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "员工姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "薪资")
    private BigDecimal salary;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
