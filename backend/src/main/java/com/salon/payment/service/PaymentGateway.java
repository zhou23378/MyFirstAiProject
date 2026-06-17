package com.salon.payment.service;

import com.salon.payment.dto.CreatePaymentReqDTO;
import com.salon.payment.dto.PaymentResultDTO;

/**
 * 支付网关接口。
 * <p>
 * TODO(N08): 当前仅有 Mock 实现，生产需对接微信支付/支付宝真实 SDK。
 * 参考：https://pay.weixin.qq.com/doc/v3/merchant/4012715700
 * </p>
 */
public interface PaymentGateway {
    PaymentResultDTO createPayment(CreatePaymentReqDTO req);
    boolean verifyCallback(String body, String signature);
}
