package com.salon.notification.listener;

import com.salon.member.entity.Member;
import com.salon.member.mapper.MemberMapper;
import com.salon.notification.enums.NotificationType;
import com.salon.notification.service.SmsService;
import com.salon.payment.event.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentNotificationListener {

    private final SmsService smsService;
    private final MemberMapper memberMapper;

    @Async("notificationExecutor")
    @EventListener
    public void onPaymentSuccess(PaymentSuccessEvent event) {
        log.info("PaymentNotificationListener: received event paymentId={}, channel={}",
            event.paymentId(), event.payChannel());

        if (event.memberId() != null) {
            try {
                Member member = memberMapper.selectById(event.memberId());
                if (member != null && member.getPhone() != null && !member.getPhone().isEmpty()) {
                    String content = "您的消费 " + (event.amount() != null ?
                        event.amount().setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "0.00") +
                        " 元已支付成功（" + event.payChannel() + "），感谢您的光临！";
                    smsService.send(member.getPhone(), NotificationType.PAYMENT_SUCCESS.getType(),
                        NotificationType.PAYMENT_SUCCESS.getTemplateCode(), content, event.memberId());
                }
            } catch (Exception e) {
                log.error("PaymentNotificationListener: failed to send notification for memberId={}",
                    event.memberId(), e);
            }
        }
    }
}
