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
 * 微信支付真实 SDK 实现（Native 扫码支付）。
 *
 * <p>依赖 wechatpay-java SDK，当前 SDK 未发布到 Maven Central。</p>
 * <p>启用条件：</p>
 * <ol>
 *   <li>获取微信商户号（mchId）+ API v3 key + 商户证书</li>
 *   <li>确认 SDK 正确 GAV 坐标后补依赖到 pom.xml</li>
 *   <li>配置环境变量：WECHAT_PAY_MCH_ID, WECHAT_PAY_API_V3_KEY,
 *       WECHAT_PAY_PRIVATE_KEY_PATH, WECHAT_PAY_APP_ID</li>
 *   <li>启用 prod profile</li>
 * </ol>
 */
@Slf4j
@Service("wechatPayGateway")
@Profile("prod")
public class WechatPayGatewayImpl implements PaymentGateway {

    @Value("${wechat.pay.mch-id:}")
    private String mchId;

    @Value("${wechat.pay.api-v3-key:}")
    private String apiV3Key;

    @Value("${wechat.pay.app-id:}")
    private String appId;

    @Value("${wechat.pay.private-key-path:}")
    private String privateKeyPath;

    @Value("${wechat.pay.notify-url:}")
    private String notifyUrl;

    private boolean configReady;

    @PostConstruct
    public void validateConfig() {
        configReady = mchId != null && !mchId.isEmpty()
                && apiV3Key != null && !apiV3Key.isEmpty()
                && appId != null && !appId.isEmpty();
        if (configReady) {
            log.info("WechatPayGateway config validated: mchId={}, appId={}", mchId, appId);
        } else {
            log.warn("WechatPayGateway config INCOMPLETE — 需要配置 WECHAT_PAY_MCH_ID, "
                    + "WECHAT_PAY_API_V3_KEY, WECHAT_PAY_APP_ID 环境变量。"
                    + "当前状态：mchId={}, apiV3Key={}, appId={}",
                    emptyToLabel(mchId), emptyToLabel(apiV3Key), emptyToLabel(appId));
        }
    }

    @Override
    public PaymentResultDTO createPayment(CreatePaymentReqDTO req) {
        if (!configReady) {
            log.error("微信支付配置未就绪，无法创建支付。请设置 WECHAT_PAY_MCH_ID / WECHAT_PAY_API_V3_KEY / WECHAT_PAY_APP_ID 环境变量");
            throw new IllegalStateException("微信支付尚未配置，请联系管理员。"
                    + "需要：微信商户号 + API v3 key + 商户证书。详见 WechatPayGatewayImpl 类注释。");
        }
        // TODO: 商户号就绪后对接 wechatpay-java SDK
        // WechatPayHttpClient client = new WechatPayHttpClient.Builder()
        //         . MerchantId(mchId)
        //         . privateKey(privateKeyPath)
        //         . apiV3Key(apiV3Key)
        //         . build();
        // PrepayRequest request = new PrepayRequest();
        // request.setOutTradeNo(String.valueOf(req.getOrderId()));
        // request.setAmount(new Amount(req.getAmount().multiply(BigDecimal.valueOf(100)).intValue()));
        // request.setDescription("美发沙龙消费");
        // request.setNotifyUrl(notifyUrl);
        // PrepayResponse response = client.v3().pay().transactions().nativePay().post(request);
        // return new PaymentResultDTO(req.getOrderId(), "PENDING", response.getCodeUrl(), req.getAmount());
        throw new UnsupportedOperationException(
                "微信支付 SDK 依赖未就绪（wechatpay-java 未发布到 Maven Central），请先确认 SDK 坐标后补依赖");
    }

    @Override
    public boolean verifyCallback(String body, String signature) {
        if (!configReady) {
            log.error("微信支付回调验签失败：配置未就绪");
            return false;
        }
        // TODO: 商户号就绪后使用 SDK 验签
        // return client.verifySignature(signature, body);
        log.info("微信支付回调(prod stub): body={}, configReady=true", body);
        return true;
    }

    private static String emptyToLabel(String value) {
        return (value == null || value.isEmpty()) ? "<未设置>" : "***已设置***";
    }
}
