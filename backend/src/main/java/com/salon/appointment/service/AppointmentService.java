package com.salon.appointment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.appointment.dto.ConvertToOrderDTO;
import com.salon.appointment.entity.Appointment;
import com.salon.appointment.vo.AppointmentPageVO;
import com.salon.appointment.vo.CalendarDayVO;
import com.salon.appointment.vo.CalendarWeekVO;
import com.salon.common.result.Result;

public interface AppointmentService extends IService<Appointment> {

    /** 分页查询预约列表（含会员名/服务名/员工名） */
    Page<AppointmentPageVO> pageWithNames(int page, int pageSize, String startDate, String endDate, Integer status);

    /** 检查技师在指定时间段是否有冲突预约，有则抛出 BusinessException */
    void checkTimeConflict(Long employeeId, java.time.LocalDate date, java.time.LocalTime startTime, java.time.LocalTime endTime, Long excludeId);

    /** 预约转消费订单 */
    Result<?> convertToOrder(Long appointmentId, ConvertToOrderDTO dto);

    /** 预约到店（状态 1→2），更新会员最后到店日期 */
    void arrive(Long appointmentId);

    /** 带状态机的状态变更：1→2/4/5, 2→3/5 */
    void updateStatus(Long id, int newStatus);

    /** 日历日视图查询 */
    CalendarDayVO getCalendarDay(String date, Long employeeId, Integer status);

    /** 日历周视图查询 */
    CalendarWeekVO getCalendarWeek(String startDate, Long employeeId);
}
