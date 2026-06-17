package com.salon.dashboard.statcard.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class StatCardSaveWrapper {
    @NotEmpty(message = "配置列表不能为空")
    @Valid
    private List<StatCardSaveDTO> items;
}
