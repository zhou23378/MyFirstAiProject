package com.salon.payment.service.impl;

import com.salon.consumption.entity.PaymentDetail;
import com.salon.payment.service.PaymentDetailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@ActiveProfiles("test")
class PaymentDetailServiceTest {

    @Autowired
    private PaymentDetailService paymentDetailService;

    @Test
    void batchInsert_shouldInsertAllRecords() {
        PaymentDetail d1 = new PaymentDetail();
        d1.setOrderId(1L);
        d1.setPayMethod(3);
        d1.setAmount(new BigDecimal("100.00"));
        d1.setPayChannel("WECHAT");

        PaymentDetail d2 = new PaymentDetail();
        d2.setOrderId(1L);
        d2.setPayMethod(2);
        d2.setAmount(new BigDecimal("50.00"));
        d2.setPayChannel("BALANCE");

        assertThatCode(() -> paymentDetailService.batchInsert(List.of(d1, d2)))
            .doesNotThrowAnyException();
    }

    @Test
    void batchInsert_shouldHandleEmptyList() {
        assertThatCode(() -> paymentDetailService.batchInsert(null))
            .doesNotThrowAnyException();
        assertThatCode(() -> paymentDetailService.batchInsert(List.of()))
            .doesNotThrowAnyException();
    }

    @Test
    void getByTransactionId_shouldReturnNullForMissingId() {
        PaymentDetail result = paymentDetailService.getByTransactionId("nonexistent");
        assertThat(result).isNull();
    }

    @Test
    void getByTransactionId_shouldReturnNullForNullOrEmpty() {
        assertThat(paymentDetailService.getByTransactionId(null)).isNull();
        assertThat(paymentDetailService.getByTransactionId("")).isNull();
    }

    @Test
    void updatePaymentStatus_shouldNotThrowForMissingRecord() {
        assertThatCode(() -> paymentDetailService.updatePaymentStatus(9999L, "TXN001", "SUCCESS"))
            .doesNotThrowAnyException();
    }
}
