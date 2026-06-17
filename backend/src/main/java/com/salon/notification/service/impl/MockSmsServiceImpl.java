package com.salon.notification.service.impl;

import com.salon.notification.entity.NotificationLog;
import com.salon.notification.mapper.NotificationLogMapper;
import com.salon.notification.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@Profile("!prod")
@RequiredArgsConstructor
public class MockSmsServiceImpl implements SmsService {

    private final NotificationLogMapper notificationLogMapper;

    @Override
    public void send(String phone, String type, String templateCode, String content) {
        send(phone, type, templateCode, content, null);
    }

    @Override
    public void send(String phone, String type, String templateCode, String content, Long memberId) {
        log.info("SMS to {} [{}]: {}", phone, type, content);
        NotificationLog logEntity = new NotificationLog();
        logEntity.setMemberId(memberId);
        logEntity.setPhone(phone);
        logEntity.setType(type);
        logEntity.setTemplateCode(templateCode);
        logEntity.setContent(content);
        logEntity.setStatus(1);
        logEntity.setSentAt(LocalDateTime.now());
        notificationLogMapper.insert(logEntity);
    }
}
