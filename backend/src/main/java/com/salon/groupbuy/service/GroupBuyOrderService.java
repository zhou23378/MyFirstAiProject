package com.salon.groupbuy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.groupbuy.dto.GroupBuyOrderReqDTO;
import com.salon.groupbuy.entity.GroupBuyOrder;
import com.salon.groupbuy.vo.GroupBuyOrderPageVO;
import com.salon.groupbuy.vo.GroupBuyOrderVO;

public interface GroupBuyOrderService extends IService<GroupBuyOrder> {

    Page<GroupBuyOrderPageVO> page(int page, int size, Long templateId, Integer status,
                                   String startDate, String endDate);

    GroupBuyOrderVO getDetail(Long id);

    GroupBuyOrderVO create(Long memberId, GroupBuyOrderReqDTO dto);

    GroupBuyOrderVO join(Long orderId, Long memberId);

    void redeem(Long participantId);

    Page<GroupBuyOrderPageVO> myOrders(Long memberId, int page, int size);
}
