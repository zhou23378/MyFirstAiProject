package com.salon.commission.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.commission.dto.CommissionSettlementReqDTO;
import com.salon.commission.entity.CommissionSettlement;
import com.salon.commission.vo.CommissionSettlementPageVO;
import com.salon.commission.vo.CommissionSettlementVO;

import java.util.List;

public interface CommissionSettlementService extends IService<CommissionSettlement> {

    Page<CommissionSettlementPageVO> page(int page, int size, Long employeeId, Integer status,
                                          String periodStart, String periodEnd);

    CommissionSettlementVO getDetail(Long id);

    List<CommissionSettlementVO> create(CommissionSettlementReqDTO dto);

    void confirm(Long id);

    void pay(Long id);

    void delete(Long id);
}
