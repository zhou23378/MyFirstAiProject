package com.salon.appointment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "日历日视图响应")
public class CalendarDayVO {

    @Schema(description = "技师列表（含当日预约）")
    private List<TechnicianCalendarVO> technicians;

    @Schema(description = "未分配技师的预约")
    private List<CalendarAppointmentVO> unassignedAppointments;

    @Schema(description = "当日统计：total/booked/arrived/completed")
    private Map<String, Integer> stats;

    @Schema(description = "营业时间配置")
    private Map<String, Object> businessHours;
}
