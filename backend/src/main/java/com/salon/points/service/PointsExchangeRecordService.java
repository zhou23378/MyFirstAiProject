package com.salon.points.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.points.dto.PointsExchangeRecordReqDTO;
import com.salon.points.entity.PointsExchangeRecord;
import com.salon.points.vo.PointsExchangeRecordVO;

public interface PointsExchangeRecordService extends IService<PointsExchangeRecord> {

    Page<PointsExchangeRecordVO> page(int page, int size, String memberPhone, Integer status,
                                       String startDate, String endDate);

    PointsExchangeRecordVO exchange(PointsExchangeRecordReqDTO dto);

    void claim(Long id);
}
