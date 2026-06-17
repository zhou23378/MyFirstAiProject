package com.salon.appointment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "日历预约视图（日历卡片展示用）")
public class CalendarAppointmentVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "服务项目ID")
    private Long serviceItemId;

    @Schema(description = "服务项目名称")
    private String serviceItemName;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "状态：1=已预约 2=已到店 3=已完成 4=已取消 5=爽约")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
