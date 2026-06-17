package com.salon.timer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.timer.entity.ServiceTimer;
import com.salon.timer.vo.ServiceTimerActiveVO;

import java.util.List;

public interface ServiceTimerService extends IService<ServiceTimer> {

    ServiceTimer start(Long appointmentId, Long queueId, Long employeeId, Long memberId,
                       Long serviceItemId, String serviceItemName, Integer plannedDuration);

    void pause(Long id);

    void resume(Long id);

    void complete(Long id, Integer actualDuration);

    List<ServiceTimerActiveVO> listActive();
}
