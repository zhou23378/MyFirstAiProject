package com.salon.inventory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.inventory.dto.StockRecordReqDTO;
import com.salon.inventory.entity.StockRecord;
import com.salon.inventory.vo.StockRecordPageVO;

public interface StockRecordService extends IService<StockRecord> {

    Page<StockRecordPageVO> pageWithNames(int page, int pageSize, Integer type);

    /** 入库/出库，自动更新 product.stock_qty */
    StockRecord createRecord(StockRecordReqDTO dto, String operator);
}
