package com.salon.payment.service.impl;

import com.salon.payment.dto.CreatePaymentReqDTO;
import com.salon.payment.dto.PaymentResultDTO;
import com.salon.payment.service.PaymentGateway;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * 支付宝真实 SDK 实现（扫码支付 / 当面付）。
 *
 * <p>依赖 alipay-sdk-java SDK，当前 SDK 未发布到 Maven Central。</p>
 * <p>启用条件：</p>
 * <ol>
 *   <li>获取支付宝商户号 + 应用公私钥</li>
 *   <li>确认 SDK 正确 GAV 坐标后补依赖到 pom.xml</li>
 *   <li>配置环境变量：ALIPAY_APP_ID, ALIPAY_PRIVATE_KEY, ALIPAY_PUBLIC_KEY</li>
 *   <li>启用 prod profile</li>
 * </ol>
 */
@Slf4j
@Service("alipayGateway")
@Profile("prod")
public class AlipayGatewayImpl implements PaymentGateway {

    @Value("${alipay.app-id:}")
    private String appId;

    @Value("${alipay.private-key:}")
    private String privateKey;

    @Value("${alipay.alipay-public-key:}")
    private String alipayPublicKey;

    @Value("${alipay.notify-url:}")
    private String notifyUrl;

    @Value("${alipay.gateway-url:https://openapi.alipay.com/gateway.do}")
    private String gatewayUrl;

    private boolean configReady;

    @PostConstruct
    public void validateConfig() {
        configReady = appId != null && !appId.isEmpty()
                && privateKey != null && !privateKey.isEmpty()
                && alipayPublicKey != null && !alipayPublicKey.isEmpty();
        if (configReady) {
            log.info("AlipayGateway config validated: appId={}, gatewayUrl={}", appId, gatewayUrl);
        } else {
            log.warn("AlipayGateway config INCOMPLETE — 需要配置 ALIPAY_APP_ID, "
                    + "ALIPAY_PRIVATE_KEY, ALIPAY_PUBLIC_KEY 环境变量。"
                    + "当前状态：appId={}, privateKey={}, publicKey={}",
                    emptyToLabel(appId), emptyToLabel(privateKey), emptyToLabel(alipayPublicKey));
        }
    }

    @Override
    public PaymentResultDTO createPayment(CreatePaymentReqDTO req) {
        if (!configReady) {
            log.error("支付宝配置未就绪，无法创建支付。请设置 ALIPAY_APP_ID / ALIPAY_PRIVATE_KEY / ALIPAY_PUBLIC_KEY 环境变量");
            throw new IllegalStateException("支付宝支付尚未配置，请联系管理员。"
                    + "需要：支付宝商户号 + 应用公私钥。详见 AlipayGatewayImpl 类注释。");
        }
        // TODO: 商户号就绪后对接 alipay-sdk-java SDK
        // AlipayClient client = new DefaultAlipayClient(
        //         gatewayUrl, appId, privateKey, "json", "UTF-8",
        //         alipayPublicKey, "RSA2");
        // AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        // request.setNotifyUrl(notifyUrl);
        // request.setBizContent(JSON.toJSONString(Map.of(
        //         "out_trade_no", String.valueOf(req.getOrderId()),
        //         "total_amount", req.getAmount().toString(),
        //         "subject", "美发沙龙消费"
        // )));
        // AlipayTradePrecreateResponse response = client.execute(request);
        // return new PaymentResultDTO(req.getOrderId(), "PENDING",
        //         response.getQrCode(), req.getAmount());
        throw new UnsupportedOperationException(
                "支付宝 SDK 依赖未就绪（alipay-sdk-java 未发布到 Maven Central），请先确认 SDK 坐标后补依赖");
    }

    @Override
    public boolean verifyCallback(String body, String signature) {
        if (!configReady) {
            log.error("支付宝回调验签失败：配置未就绪");
            return false;
        }
        // TODO: 商户号就绪后使用 SDK 验签
        log.info("支付宝回调(prod stub): body={}, configReady=true", body);
        return true;
    }

    private static String emptyToLabel(String value) {
        return (value == null || value.isEmpty()) ? "<未设置>" : "***已设置***";
    }
}
