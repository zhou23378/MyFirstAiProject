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
@Schema(description = "日历周视图响应")
public class CalendarWeekVO {

    @Schema(description = "本周7天数据")
    private List<CalendarWeekDayVO> days;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "周视图中的单天数据")
    public static class CalendarWeekDayVO {

        @Schema(description = "日期（yyyy-MM-dd）")
        private String date;

        @Schema(description = "星期几（周一/周二/...）")
        private String dayOfWeek;

        @Schema(description = "预约总数")
        private int totalCount;

        @Schema(description = "各状态数量：booked/arrived/completed/cancelled/noshow")
        private Map<String, Integer> statusCounts;

        @Schema(description = "当天预约列表（最多返回4条用于预览）")
        private List<CalendarAppointmentVO> appointments;
    }
}
