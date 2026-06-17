package com.salon.notification.enums;

import lombok.Getter;

@Getter
public enum NotificationType {

    BIRTHDAY("BIRTHDAY", "SMS_BIRTHDAY_TPL"),
    LOGIN_CODE("LOGIN_CODE", null),
    APPOINTMENT_REMINDER("APPOINTMENT_REMINDER", "SMS_APPOINT_REMINDER_TPL"),
    PAYMENT_SUCCESS("PAYMENT_SUCCESS", "SMS_PAYMENT_SUCCESS_TPL"),
    RECHARGE("RECHARGE", "SMS_RECHARGE_TPL"),
    QUEUE_NOTIFY("QUEUE_NOTIFY", "SMS_QUEUE_NOTIFY_TPL");

    private final String type;
    private final String templateCode;

    NotificationType(String type, String templateCode) {
        this.type = type;
        this.templateCode = templateCode;
    }
}
