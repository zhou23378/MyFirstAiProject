package com.salon.consumption.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.consumption.dto.CreateOrderDTO;
import com.salon.consumption.dto.ResumeOrderReqDTO;
import com.salon.consumption.dto.SuspendOrderReqDTO;
import com.salon.consumption.entity.ConsumptionOrder;
import com.salon.consumption.vo.SuspendedOrderVO;

import java.util.List;

/**
 * 消费订单 Service 接口
 * <p>
 * 定义消费订单的业务方法
 * </p>
 */
public interface ConsumptionOrderService extends IService<ConsumptionOrder> {

    /**
     * 创建消费订单
     * <p>
     * 包含订单主表和明细表的创建，以及会员余额和积分的更新
     * </p>
     *
     * @param dto 创建订单请求
     * @return 创建的订单
     */
    ConsumptionOrder createOrder(CreateOrderDTO dto);

    ConsumptionOrder refundOrder(Long id);

    /** 挂单：保存为草稿（status=0），不处理支付 */
    ConsumptionOrder suspendOrder(SuspendOrderReqDTO dto);

    /** 查询所有挂起订单 */
    List<SuspendedOrderVO> listSuspended();

    /** 取单结算：加载草稿 → 完成支付 → status=0→1 */
    ConsumptionOrder resumeOrder(Long id, ResumeOrderReqDTO dto);
}
