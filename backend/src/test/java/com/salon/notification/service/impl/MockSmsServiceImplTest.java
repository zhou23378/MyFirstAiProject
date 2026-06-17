package com.salon.notification.service.impl;

import com.salon.notification.entity.NotificationLog;
import com.salon.notification.mapper.NotificationLogMapper;
import com.salon.notification.service.SmsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MockSmsServiceImplTest {

    @Autowired
    private SmsService smsService;

    @Autowired
    private NotificationLogMapper notificationLogMapper;

    @Test
    void send_shouldInsertNotificationLog() {
        smsService.send("13800138000", "LOGIN_CODE", null, "您的验证码是123456", 1L);

        // 由于 MockSmsServiceImpl 在 !prod profile 生效，test profile 也应该走 Mock
        // 验证 notification_log 中已插入记录
        var logs = notificationLogMapper.selectList(null);
        assertThat(logs).isNotEmpty();
        NotificationLog log = logs.get(0);
        assertThat(log.getPhone()).isEqualTo("13800138000");
        assertThat(log.getType()).isEqualTo("LOGIN_CODE");
        assertThat(log.getContent()).contains("123456");
        assertThat(log.getStatus()).isEqualTo(1);
    }
}
