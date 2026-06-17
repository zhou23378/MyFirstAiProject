package com.salon.inventory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.inventory.dto.ReturnOrderReqDTO;
import com.salon.inventory.entity.ReturnOrder;
import com.salon.inventory.vo.ReturnOrderPageVO;
import com.salon.inventory.vo.ReturnOrderVO;

public interface ReturnOrderService extends IService<ReturnOrder> {

    ReturnOrderVO create(ReturnOrderReqDTO dto, String applicant);

    ReturnOrderVO update(Long id, ReturnOrderReqDTO dto);

    ReturnOrderVO getDetail(Long id);

    Page<ReturnOrderPageVO> page(int page, int pageSize, Integer status, Long supplierId);

    void submit(Long id);

    void approve(Long id, String approver);

    void reject(Long id, String approver);

    void complete(Long id);

    void cancel(Long id);

    void delete(Long id);
}
