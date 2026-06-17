package com.salon.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付方式枚举
 */
@Getter
@AllArgsConstructor
public enum PayMethodEnum {

    CASH(1, "现金"),
    BALANCE(2, "余额"),
    WECHAT(3, "微信"),
    ALIPAY(4, "支付宝"),
    BANK_CARD(5, "银行卡"),
    STORED_CARD(6, "储值卡"),
    GROUPON(7, "团购券"),
    MIXED(8, "混合");

    @EnumValue
    private final Integer value;

    @JsonValue
    private final String label;

    /**
     * 根据值获取枚举
     */
    public static PayMethodEnum of(Integer value) {
        if (value == null) return CASH;
        for (PayMethodEnum e : values()) {
            if (e.value.equals(value)) return e;
        }
        return CASH;
    }
}
