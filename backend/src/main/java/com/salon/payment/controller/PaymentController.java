package com.salon.payment.controller;

import com.salon.common.result.Result;
import com.salon.consumption.entity.PaymentDetail;
import com.salon.customer.service.CustomerAuthService;
import com.salon.payment.dto.CreatePaymentReqDTO;
import com.salon.payment.dto.PaymentResultDTO;
import com.salon.payment.service.PaymentDetailService;
import com.salon.payment.service.PaymentGateway;
import com.salon.payment.vo.PaymentStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "顾客支付", description = "顾客端支付接口")
@RestController
@RequestMapping("/api/customer/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final CustomerAuthService authService;

    @Qualifier("wechatPayGateway")
    private final PaymentGateway wechatPayGateway;

    @Qualifier("alipayGateway")
    private final PaymentGateway alipayGateway;

    private final PaymentDetailService paymentDetailService;

    @Operation(summary = "创建支付")
    @PostMapping("/create")
    public Result<PaymentResultDTO> create(@Valid @RequestBody CreatePaymentReqDTO req,
                                           @RequestHeader("X-Customer-Token") String token) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        PaymentGateway gateway = "ALIPAY".equals(req.getMethod()) ? alipayGateway : wechatPayGateway;
        return Result.success(gateway.createPayment(req));
    }

    @Operation(summary = "查询支付状态")
    @GetMapping("/{id}/status")
    public Result<PaymentStatusVO> status(@PathVariable Long id,
                                           @RequestHeader("X-Customer-Token") String token) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        PaymentDetail detail = paymentDetailService.getByTransactionId("PAY" + id);
        String status = detail != null ? (detail.getTransactionId() != null ? "SUCCESS" : "PENDING") : "PENDING";
        return Result.success(new PaymentStatusVO(status));
    }
}
