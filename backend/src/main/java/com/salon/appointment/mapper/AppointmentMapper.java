package com.salon.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.salon.appointment.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
}
