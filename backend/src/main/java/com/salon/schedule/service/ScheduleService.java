package com.salon.schedule.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.schedule.entity.Schedule;
import com.salon.schedule.vo.ScheduleWeekVO;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService extends IService<Schedule> {

    List<ScheduleWeekVO> weekView(LocalDate startDate, LocalDate endDate);

    void batchSet(Long employeeId, List<Schedule> schedules);
}
