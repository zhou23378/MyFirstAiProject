package com.salon.dailyclose.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.dailyclose.dto.DailyCloseReqDTO;
import com.salon.dailyclose.entity.DailyClose;
import com.salon.dailyclose.vo.DailyCloseSummaryVO;

public interface DailyCloseService extends IService<DailyClose> {

    DailyCloseSummaryVO getTodaySummary();

    DailyClose submitEntry(DailyCloseReqDTO dto);

    DailyClose lock(Long id, String lockedBy);
}
