package com.salon.notification.service.impl;

import com.salon.notification.entity.NotificationLog;
import com.salon.notification.mapper.NotificationLogMapper;
import com.salon.notification.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@Profile("prod")
@Primary
@RequiredArgsConstructor
public class AliyunSmsServiceImpl implements SmsService {

    private final NotificationLogMapper notificationLogMapper;

    @Value("${aliyun.sms.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name:美发沙龙}")
    private String signName;

    @Value("${aliyun.sms.endpoint:dysmsapi.aliyuncs.com}")
    private String endpoint;

    @Override
    public void send(String phone, String type, String templateCode, String content) {
        send(phone, type, templateCode, content, null);
    }

    @Override
    public void send(String phone, String type, String templateCode, String content, Long memberId) {
        if (accessKeyId.isEmpty() || accessKeySecret.isEmpty()) {
            log.warn("Aliyun SMS credentials not configured, falling back to log-only mode. Set ALIYUN_ACCESS_KEY_ID and ALIYUN_ACCESS_KEY_SECRET.");
            fallbackLog(phone, type, templateCode, content, memberId);
            return;
        }

        NotificationLog logEntity = new NotificationLog();
        logEntity.setMemberId(memberId);
        logEntity.setPhone(phone);
        logEntity.setType(type);
        logEntity.setTemplateCode(templateCode);
        logEntity.setContent(content);
        logEntity.setStatus(0);
        logEntity.setSentAt(LocalDateTime.now());
        notificationLogMapper.insert(logEntity);

        try {
            com.aliyun.dysmsapi20170525.Client client = createClient();
            com.aliyun.dysmsapi20170525.models.SendSmsRequest req =
                    new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                            .setPhoneNumbers(phone)
                            .setSignName(signName)
                            .setTemplateCode(templateCode)
                            .setTemplateParam("{\"content\":\"" + content + "\"}");
            com.aliyun.dysmsapi20170525.models.SendSmsResponse resp = client.sendSms(req);

            if ("OK".equals(resp.getBody().getCode())) {
                logEntity.setStatus(1);
                log.info("SMS sent to {} [{}]: {}", phone, type, resp.getBody().getBizId());
            } else {
                logEntity.setStatus(2);
                logEntity.setErrorMsg(resp.getBody().getMessage());
                log.error("SMS failed to {} [{}]: {}", phone, type, resp.getBody().getMessage());
            }
        } catch (Exception e) {
            logEntity.setStatus(2);
            logEntity.setErrorMsg(e.getMessage());
            log.error("SMS exception to {} [{}]: {}", phone, type, e.getMessage());
        }
        notificationLogMapper.updateById(logEntity);
    }

    private void fallbackLog(String phone, String type, String templateCode, String content, Long memberId) {
        NotificationLog logEntity = new NotificationLog();
        logEntity.setMemberId(memberId);
        logEntity.setPhone(phone);
        logEntity.setType(type);
        logEntity.setTemplateCode(templateCode);
        logEntity.setContent(content);
        logEntity.setStatus(1);
        logEntity.setSentAt(LocalDateTime.now());
        notificationLogMapper.insert(logEntity);
        log.info("SMS (fallback) to {} [{}]: {}", phone, type, content);
    }

    private com.aliyun.dysmsapi20170525.Client createClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);
        config.endpoint = endpoint;
        return new com.aliyun.dysmsapi20170525.Client(config);
    }
}
