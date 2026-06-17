package com.salon.payment.service.impl;

import com.salon.payment.dto.CreatePaymentReqDTO;
import com.salon.payment.dto.PaymentResultDTO;
import com.salon.payment.service.PaymentGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 微信支付 Mock 实现。
 * TODO(N08): 替换为真实微信支付 SDK（JSAPI/NATIVE/H5），需商户号+mchid+证书。
 */
@Slf4j
@Service("wechatPayGateway")
@Profile("!prod")
public class MockWechatPayGateway implements PaymentGateway {

    private final ConcurrentMap<Long, String> paymentStore = new ConcurrentHashMap<>();

    @Override
    public PaymentResultDTO createPayment(CreatePaymentReqDTO req) {
        String qrCode = "weixin://wxpay/bizpayurl?pr=" + UUID.randomUUID().toString().replace("-", "");
        long paymentId = System.currentTimeMillis();
        paymentStore.put(paymentId, "PENDING");
        log.info("微信支付创建(模拟) - 订单: {}, 金额: {}, QR: {}", req.getOrderId(), req.getAmount(), qrCode);
        return new PaymentResultDTO(paymentId, "PENDING", qrCode, req.getAmount());
    }

    @Override
    public boolean verifyCallback(String body, String signature) {
        log.info("微信支付回调验证(模拟) - body: {}", body);
        return true;
    }
}
