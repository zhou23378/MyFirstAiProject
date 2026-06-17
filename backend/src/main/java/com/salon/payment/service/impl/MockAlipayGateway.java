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
 * 支付宝支付 Mock 实现。
 * TODO(N08): 替换为真实支付宝 SDK（当面付/扫码付），需配置 appId+私钥+公钥。
 */
@Slf4j
@Service("alipayGateway")
@Profile("!prod")
public class MockAlipayGateway implements PaymentGateway {

    private final ConcurrentMap<Long, String> paymentStore = new ConcurrentHashMap<>();

    @Override
    public PaymentResultDTO createPayment(CreatePaymentReqDTO req) {
        String qrCode = "https://qr.alipay.com/bax" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        long paymentId = System.currentTimeMillis();
        paymentStore.put(paymentId, "PENDING");
        log.info("支付宝支付创建(模拟) - 订单: {}, 金额: {}, QR: {}", req.getOrderId(), req.getAmount(), qrCode);
        return new PaymentResultDTO(paymentId, "PENDING", qrCode, req.getAmount());
    }

    @Override
    public boolean verifyCallback(String body, String signature) {
        log.info("支付宝回调验证(模拟) - body: {}", body);
        return true;
    }
}
