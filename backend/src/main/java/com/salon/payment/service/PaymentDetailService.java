package com.salon.payment.service;

import com.salon.consumption.entity.PaymentDetail;

import java.util.List;

public interface PaymentDetailService {
    void batchInsert(List<PaymentDetail> details);
    void updatePaymentStatus(Long id, String transactionId, String status);
    PaymentDetail getByTransactionId(String transactionId);
}
