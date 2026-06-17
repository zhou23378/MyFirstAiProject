package com.salon.notification.service;

public interface SmsService {

    void send(String phone, String type, String templateCode, String content);

    void send(String phone, String type, String templateCode, String content, Long memberId);
}
