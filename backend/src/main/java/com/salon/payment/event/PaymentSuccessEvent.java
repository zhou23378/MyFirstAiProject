package com.salon.payment.event;

import java.math.BigDecimal;

public record PaymentSuccessEvent(Long paymentId, Long orderId, Long memberId, BigDecimal amount, String payChannel) {
}
