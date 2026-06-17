package com.salon.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    NORMAL(1, "正常"),
    REFUNDED(2, "已退款");

    @EnumValue
    private final Integer value;

    @JsonValue
    private final String label;

    /**
     * 根据值获取枚举
     */
    public static OrderStatusEnum of(Integer value) {
        if (value == null) return NORMAL;
        for (OrderStatusEnum e : values()) {
            if (e.value.equals(value)) return e;
        }
        return NORMAL;
    }
}
