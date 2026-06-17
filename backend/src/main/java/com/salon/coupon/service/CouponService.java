package com.salon.coupon.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.coupon.entity.Coupon;
import com.salon.coupon.vo.CouponPageVO;

import java.math.BigDecimal;

public interface CouponService extends IService<Coupon> {

    Page<CouponPageVO> pageWithNames(int page, int pageSize, Long memberId, Integer status);

    Coupon issue(Long templateId, Long memberId);

    /** 核销优惠券，返回优惠金额 */
    BigDecimal verify(String code, Long orderId, BigDecimal orderAmount);
}
