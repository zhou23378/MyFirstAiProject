package com.salon.payment.controller;

import com.salon.payment.event.PaymentSuccessEvent;
import com.salon.payment.service.PaymentGateway;
import com.salon.payment.service.PaymentDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Tag(name = "支付回调", description = "第三方支付异步通知接收")
@RestController
@RequestMapping("/api/payment/callback")
@RequiredArgsConstructor
public class PaymentCallbackController {

    @Qualifier("wechatPayGateway")
    private final PaymentGateway wechatPayGateway;

    @Qualifier("alipayGateway")
    private final PaymentGateway alipayGateway;

    private final PaymentDetailService paymentDetailService;
    private final ApplicationEventPublisher eventPublisher;

    /** 回调幂等去重集合（Mock 实现使用内存；生产需改用 Redis SETNX） */
    private final Set<String> processedCallbacks = ConcurrentHashMap.newKeySet();

    @Operation(summary = "微信支付回调")
    @PostMapping("/wechat")
    // N09: 回调接口由微信支付官方调用，返回格式必须遵守微信规范（{"code":"SUCCESS","message":"OK"}），
    // 不能包装为 Result<>，否则微信服务器会认为回调失败并重复通知。
    public Map<String, String> wechatCallback(@RequestBody String body,
                                               @RequestHeader(value = "Wechatpay-Signature", required = false) String signature) {
        log.info("微信支付回调: {}", body);

        String callbackId = extractTransactionId(body, "transaction_id");
        if (!processedCallbacks.add(callbackId)) {
            log.warn("重复回调已忽略: callbackId={}", callbackId);
            return Map.of("code", "SUCCESS", "message", "OK");
        }

        boolean valid = wechatPayGateway.verifyCallback(body, signature);
        if (valid) {
            publishPaymentSuccess(body, "WECHAT");
        }
        return valid ? Map.of("code", "SUCCESS", "message", "OK")
                : Map.of("code", "FAIL", "message", "签名验证失败");
    }

    @Operation(summary = "支付宝回调")
    @PostMapping("/alipay")
    // N09: 回调接口由支付宝官方调用，返回格式必须遵守支付宝规范（"success"/"fail"），
    // 不能包装为 Result<>，否则支付宝服务器会认为回调失败并重复通知。
    public String alipayCallback(@RequestBody String body) {
        log.info("支付宝回调: {}", body);

        String callbackId = extractTransactionId(body, "out_trade_no");
        if (!processedCallbacks.add(callbackId)) {
            log.warn("重复回调已忽略: callbackId={}", callbackId);
            return "success";
        }

        boolean valid = alipayGateway.verifyCallback(body, null);
        if (valid) {
            publishPaymentSuccess(body, "ALIPAY");
        }
        return valid ? "success" : "fail";
    }

    private void publishPaymentSuccess(String body, String payChannel) {
        try {
            String txnId = extractTransactionId(body, payChannel.equals("WECHAT") ? "transaction_id" : "out_trade_no");
            eventPublisher.publishEvent(new PaymentSuccessEvent(null, null, null, null, payChannel));
            log.info("PaymentSuccessEvent published: txnId={}, channel={}", txnId, payChannel);
        } catch (Exception e) {
            log.error("Failed to publish PaymentSuccessEvent", e);
        }
    }

    /** 从回调 body 中提取幂等键，解析失败则降级使用 body hashCode */
    private String extractTransactionId(String body, String fieldName) {
        if (body == null || body.isBlank()) return "";
        Pattern p = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(body);
        if (m.find()) return fieldName + ":" + m.group(1);
        return "fallback:" + body.hashCode();
    }
}
