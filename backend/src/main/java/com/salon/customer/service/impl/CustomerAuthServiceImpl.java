package com.salon.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salon.common.exception.BusinessException;
import com.salon.customer.dto.CustomerLoginReqDTO;
import com.salon.customer.dto.CustomerLoginRespDTO;
import com.salon.customer.entity.CustomerSession;
import com.salon.customer.mapper.CustomerSessionMapper;
import com.salon.customer.service.CustomerAuthService;
import com.salon.member.entity.Member;
import com.salon.member.mapper.MemberMapper;
import com.salon.notification.entity.NotificationLog;
import com.salon.notification.enums.NotificationType;
import com.salon.notification.mapper.NotificationLogMapper;
import com.salon.notification.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerAuthServiceImpl implements CustomerAuthService {

    private final MemberMapper memberMapper;
    private final CustomerSessionMapper sessionMapper;
    private final NotificationLogMapper notificationLogMapper;
    private final SmsService smsService;

    /** 手机号 → 最近一次成功发送验证码的时间（仅对成功发送计次） */
    private final Map<String, Long> lastSendTime = new ConcurrentHashMap<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendCode(String phone) {
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>().eq(Member::getPhone, phone));
        if (member == null) {
            throw BusinessException.badRequest("该手机号未注册，请先到店登记");
        }

        // 限流检查放在所有验证通过之后，只对成功发送计次
        long now = System.currentTimeMillis();
        Long last = lastSendTime.get(phone);
        if (last != null && (now - last) < 60_000L) {
            throw BusinessException.badRequest("验证码发送过于频繁，请稍后再试（每分钟最多1次）");
        }

        // 生成6位验证码
        String code = String.format("%06d", (int)(Math.random() * 1000000));
        // 发送短信
        smsService.send(phone, NotificationType.LOGIN_CODE.getType(), null, "【美发沙龙】您的验证码是：" + code + "，5分钟内有效", member.getId());
        // 记录通知日志
        NotificationLog notifLog = new NotificationLog();
        notifLog.setMemberId(member.getId());
        notifLog.setPhone(phone);
        notifLog.setType(NotificationType.LOGIN_CODE.getType());
        notifLog.setContent(sha256(code));
        notifLog.setStatus(1);
        notifLog.setSentAt(LocalDateTime.now());
        notificationLogMapper.insert(notifLog);

        // 成功发送后才记录时间（失败不计次）
        lastSendTime.put(phone, now);

        log.info("验证码已发送至 phone={}", phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerLoginRespDTO login(CustomerLoginReqDTO req) {
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>().eq(Member::getPhone, req.getPhone()));
        if (member == null) {
            throw BusinessException.badRequest("手机号未注册");
        }

        boolean valid = false;
        String loginMethod;

        if (req.getCode() != null && !req.getCode().isEmpty()) {
            loginMethod = "SMS_CODE";
            // 检查最近一条验证码记录
            NotificationLog lastCode = notificationLogMapper.selectOne(
                    new LambdaQueryWrapper<NotificationLog>()
                            .eq(NotificationLog::getPhone, req.getPhone())
                            .eq(NotificationLog::getType, NotificationType.LOGIN_CODE.getType())
                            .orderByDesc(NotificationLog::getId)
                            .last("LIMIT 1"));
            if (lastCode != null) {
                String storedHash = lastCode.getContent();
                String submittedHash = sha256(req.getCode());
                if (storedHash.equals(submittedHash)) {
                    // 5分钟有效期
                    if (lastCode.getSentAt().plusMinutes(5).isAfter(LocalDateTime.now())) {
                        valid = true;
                    } else {
                        throw BusinessException.badRequest("验证码已过期，请重新获取");
                    }
                }
            }
            if (!valid) {
                throw BusinessException.badRequest("验证码错误");
            }
        } else {
            throw BusinessException.badRequest("请提供验证码");
        }

        // 生成 session token
        String token = UUID.randomUUID().toString().replace("-", "");
        CustomerSession session = new CustomerSession();
        session.setMemberId(member.getId());
        session.setToken(token);
        session.setLoginMethod(loginMethod);
        session.setExpireAt(LocalDateTime.now().plusDays(7));
        sessionMapper.insert(session);

        return new CustomerLoginRespDTO(token, member.getId(), member.getName(), member.getPhone());
    }

    @Override
    public void logout(String token) {
        sessionMapper.delete(new LambdaQueryWrapper<CustomerSession>().eq(CustomerSession::getToken, token));
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    @Override
    public Long getMemberIdByToken(String token) {
        CustomerSession session = sessionMapper.selectOne(
                new LambdaQueryWrapper<CustomerSession>().eq(CustomerSession::getToken, token));
        if (session == null || session.getExpireAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        return session.getMemberId();
    }
}
