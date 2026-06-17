package com.salon.appointment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "技师日历列视图（日视图中的一列）")
public class TechnicianCalendarVO {

    @Schema(description = "技师ID")
    private Long id;

    @Schema(description = "技师姓名")
    private String name;

    @Schema(description = "实时状态：AVAILABLE|BUSY|BREAK|OFF_DUTY")
    private String status;

    @Schema(description = "该技师当天的预约列表")
    private List<CalendarAppointmentVO> appointments;
}
