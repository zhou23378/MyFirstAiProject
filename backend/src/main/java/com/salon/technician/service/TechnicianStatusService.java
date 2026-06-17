package com.salon.technician.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.technician.entity.TechnicianStatus;
import com.salon.technician.vo.TechnicianStatusVO;

import java.util.List;

public interface TechnicianStatusService extends IService<TechnicianStatus> {

    List<TechnicianStatusVO> listWithEmployee();

    TechnicianStatusVO getByEmployeeId(Long employeeId);

    void changeStatus(Long employeeId, String status, String currentCustomerName, String currentServiceName);
}
