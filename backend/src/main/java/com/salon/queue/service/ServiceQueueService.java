package com.salon.queue.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.queue.entity.ServiceQueue;
import com.salon.queue.vo.QueueTodayStatsVO;
import com.salon.queue.vo.ServiceQueueItemVO;

import java.util.List;

public interface ServiceQueueService extends IService<ServiceQueue> {

    List<ServiceQueueItemVO> listToday();

    ServiceQueue enqueue(Long memberId, String memberName, Long serviceItemId, String serviceItemName);

    void assign(Long queueId, Long employeeId);

    void skip(Long id);

    void cancel(Long id);

    QueueTodayStatsVO todayStats();

    /**
     * 查询今日最早等待中的排队记录
     * @return 下一个等待顾客，无则返回 null
     */
    ServiceQueue findNextWaitingToday();
}
