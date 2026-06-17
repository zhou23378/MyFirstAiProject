package com.salon.appointment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salon.appointment.dto.AppointmentReqDTO;
import com.salon.appointment.dto.AppointmentStatusDTO;
import com.salon.appointment.dto.ConvertToOrderDTO;
import com.salon.appointment.entity.Appointment;
import com.salon.appointment.service.AppointmentService;
import com.salon.appointment.vo.AppointmentPageVO;
import com.salon.appointment.vo.AppointmentVO;
import com.salon.appointment.vo.CalendarDayVO;
import com.salon.appointment.vo.CalendarWeekVO;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import com.salon.consumption.service.ConsumptionOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "预约管理", description = "预约的增删改查接口")
@RestController
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Deprecated(since = "微服务迁移后移除")
    private final ConsumptionOrderService consumptionOrderService;

    @GetMapping("/page")
    @Operation(summary = "分页查询预约列表（含会员名/服务名/员工名）")
    public Result<PageResult<AppointmentPageVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer status) {
        Page<AppointmentPageVO> p = appointmentService.pageWithNames(page, pageSize, startDate, endDate, status);
        return Result.success(PageResult.of(p));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询预约详情")
    public Result<AppointmentVO> getById(@PathVariable Long id) {
        Appointment entity = appointmentService.getById(id);
        if (entity == null) {
            return Result.notFound("预约不存在");
        }
        return Result.success(AppointmentVO.from(entity));
    }

    @PostMapping
    @Operation(summary = "新增预约")
    public Result<AppointmentVO> create(@Valid @RequestBody AppointmentReqDTO dto) {
        appointmentService.checkTimeConflict(dto.getEmployeeId(), dto.getAppointmentDate(),
            dto.getStartTime(), dto.getEndTime(), null);
        Appointment entity = new Appointment();
        BeanUtils.copyProperties(dto, entity);
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        appointmentService.save(entity);
        return Result.success(AppointmentVO.from(entity));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改预约")
    public Result<AppointmentVO> update(@PathVariable Long id, @Valid @RequestBody AppointmentReqDTO dto) {
        Appointment entity = appointmentService.getById(id);
        if (entity == null) {
            return Result.notFound("预约不存在");
        }
        appointmentService.checkTimeConflict(dto.getEmployeeId(), dto.getAppointmentDate(),
            dto.getStartTime(), dto.getEndTime(), id);
        BeanUtils.copyProperties(dto, entity, "id", "createTime", "updateTime");
        appointmentService.updateById(entity);
        return Result.success(AppointmentVO.from(entity));
    }

    @PutMapping("/{id}/arrive")
    @Operation(summary = "预约到店（状态 1→2）")
    public Result<Void> arrive(@PathVariable Long id) {
        appointmentService.arrive(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新预约状态（带状态机校验）")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody AppointmentStatusDTO dto) {
        appointmentService.updateStatus(id, dto.getStatus());
        return Result.success();
    }

    @PostMapping("/{id}/convert-to-order")
    @Operation(summary = "预约转消费订单")
    public Result<?> convertToOrder(@PathVariable Long id, @Valid @RequestBody ConvertToOrderDTO dto) {
        return appointmentService.convertToOrder(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除预约")
    public Result<Void> delete(@PathVariable Long id) {
        appointmentService.removeById(id);
        return Result.success();
    }

    @GetMapping("/calendar/day")
    @Operation(summary = "日历日视图（技师列 × 时间轴 + 预约卡片）")
    public Result<CalendarDayVO> calendarDay(
            @RequestParam String date,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer status) {
        return Result.success(appointmentService.getCalendarDay(date, employeeId, status));
    }

    @GetMapping("/calendar/week")
    @Operation(summary = "日历周视图（7天概览）")
    public Result<CalendarWeekVO> calendarWeek(
            @RequestParam String startDate,
            @RequestParam(required = false) Long employeeId) {
        return Result.success(appointmentService.getCalendarWeek(startDate, employeeId));
    }
}
