package com.salon.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import com.salon.consumption.entity.PaymentDetail;
import com.salon.consumption.mapper.PaymentDetailMapper;
import com.salon.payment.service.PaymentDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentDetailServiceImpl implements PaymentDetailService {

    private final JdbcTemplate jdbcTemplate;
    private final PaymentDetailMapper paymentDetailMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchInsert(List<PaymentDetail> details) {
        if (details == null || details.isEmpty()) return;
        List<Object[]> batchArgs = new ArrayList<>();
        for (PaymentDetail d : details) {
            batchArgs.add(new Object[]{d.getOrderId(), d.getPayMethod(), d.getAmount(), d.getPayChannel()});
        }
        int[] results = jdbcTemplate.batchUpdate(
            "INSERT INTO payment_detail (order_id, pay_method, amount, pay_channel) VALUES (?, ?, ?, ?)",
            batchArgs);
        int successCount = 0;
        for (int r : results) {
            if (r == 1 || r == Statement.SUCCESS_NO_INFO) successCount++;
        }
        if (successCount != details.size()) {
            throw new BusinessException(ErrorCode.PAYMENT_DETAIL_WRITE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePaymentStatus(Long id, String transactionId, String status) {
        int rows = jdbcTemplate.update(
            "UPDATE payment_detail SET transaction_id = ? WHERE id = ?",
            transactionId, id);
        if (rows == 0) {
            log.warn("PaymentDetail update failed: id={}", id);
        }
    }

    @Override
    public PaymentDetail getByTransactionId(String transactionId) {
        if (transactionId == null || transactionId.isEmpty()) return null;
        return paymentDetailMapper.selectOne(
            new LambdaQueryWrapper<PaymentDetail>().eq(PaymentDetail::getTransactionId, transactionId));
    }
}
