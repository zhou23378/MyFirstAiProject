package com.salon.notification.listener;

import com.salon.member.entity.Member;
import com.salon.member.mapper.MemberMapper;
import com.salon.notification.enums.NotificationType;
import com.salon.notification.service.SmsService;
import com.salon.payment.event.PaymentSuccessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentNotificationListenerTest {

    @Mock
    private SmsService smsService;

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private PaymentNotificationListener listener;

    @BeforeEach
    void setUp() {
        // Spring @Async won't execute in unit test, listener runs synchronously
    }

    @Test
    void onPaymentSuccess_shouldSendSmsWhenMemberHasPhone() {
        Member member = new Member();
        member.setId(1L);
        member.setPhone("13800138000");
        when(memberMapper.selectById(1L)).thenReturn(member);

        PaymentSuccessEvent event = new PaymentSuccessEvent(100L, 1L, 1L,
            new BigDecimal("200.00"), "WECHAT");

        listener.onPaymentSuccess(event);

        ArgumentCaptor<String> phoneCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(smsService).send(phoneCaptor.capture(),
            eq(NotificationType.PAYMENT_SUCCESS.getType()),
            eq(NotificationType.PAYMENT_SUCCESS.getTemplateCode()),
            contentCaptor.capture(),
            eq(1L));

        assertThat(phoneCaptor.getValue()).isEqualTo("13800138000");
        assertThat(contentCaptor.getValue()).contains("200.00").contains("WECHAT");
    }

    @Test
    void onPaymentSuccess_shouldNotSendWhenMemberHasNoPhone() {
        Member member = new Member();
        member.setId(1L);
        member.setPhone("");
        when(memberMapper.selectById(1L)).thenReturn(member);

        PaymentSuccessEvent event = new PaymentSuccessEvent(100L, 1L, 1L,
            new BigDecimal("100.00"), "ALIPAY");

        listener.onPaymentSuccess(event);

        verify(smsService, never()).send(any(), any(), any(), any(), any());
    }

    @Test
    void onPaymentSuccess_shouldHandleNullMemberGracefully() {
        when(memberMapper.selectById(1L)).thenReturn(null);

        PaymentSuccessEvent event = new PaymentSuccessEvent(100L, 1L, 1L,
            new BigDecimal("100.00"), "WECHAT");

        // 不应抛异常
        listener.onPaymentSuccess(event);

        verify(smsService, never()).send(any(), any(), any(), any(), any());
    }

    @Test
    void onPaymentSuccess_shouldHandleNullMemberIdGracefully() {
        PaymentSuccessEvent event = new PaymentSuccessEvent(100L, 1L, null,
            new BigDecimal("100.00"), "WECHAT");

        // 不应抛异常
        listener.onPaymentSuccess(event);

        verify(smsService, never()).send(any(), any(), any(), any(), any());
    }
}
