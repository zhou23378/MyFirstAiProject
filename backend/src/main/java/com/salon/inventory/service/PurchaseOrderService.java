package com.salon.inventory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.inventory.dto.PurchaseOrderReqDTO;
import com.salon.inventory.entity.PurchaseOrder;
import com.salon.inventory.vo.PurchaseOrderPageVO;
import com.salon.inventory.vo.PurchaseOrderVO;

public interface PurchaseOrderService extends IService<PurchaseOrder> {

    PurchaseOrderVO create(PurchaseOrderReqDTO dto, String applicant);

    PurchaseOrderVO update(Long id, PurchaseOrderReqDTO dto);

    PurchaseOrderVO getDetail(Long id);

    Page<PurchaseOrderPageVO> page(int page, int pageSize, Integer status, Long supplierId);

    void submit(Long id);

    void approve(Long id, String approver);

    void receive(Long id);

    void cancel(Long id);

    void delete(Long id);
}
