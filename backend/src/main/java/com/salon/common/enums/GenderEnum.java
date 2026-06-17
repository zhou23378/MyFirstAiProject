package com.salon.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举
 */
@Getter
@AllArgsConstructor
public enum GenderEnum {

    UNKNOWN(0, "未知"),
    MALE(1, "男"),
    FEMALE(2, "女");

    @EnumValue
    private final Integer value;

    @JsonValue
    private final String label;

    /**
     * 根据值获取枚举
     */
    public static GenderEnum of(Integer value) {
        if (value == null) return UNKNOWN;
        for (GenderEnum e : values()) {
            if (e.value.equals(value)) return e;
        }
        return UNKNOWN;
    }
}
