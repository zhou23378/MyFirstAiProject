package com.salon.schedule.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.schedule.entity.Attendance;
import com.salon.schedule.vo.AttendancePageVO;

public interface AttendanceService extends IService<Attendance> {

    Page<AttendancePageVO> pageWithNames(int page, int pageSize, String date, Long employeeId);

    Attendance clock(Long employeeId, Integer type);
}
