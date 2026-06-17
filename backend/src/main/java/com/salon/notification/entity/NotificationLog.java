package com.salon.notification.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification_log")
public class NotificationLog extends BaseEntity {
    private Long memberId;
    private String phone;
    private String type;
    private String templateCode;
    private String content;
    private Integer status;
    private String errorMsg;
    private LocalDateTime sentAt;
    private Integer isRead;
}
