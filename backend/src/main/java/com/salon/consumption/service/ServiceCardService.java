package com.salon.consumption.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.consumption.entity.ServiceCard;

public interface ServiceCardService extends IService<ServiceCard> {

    ServiceCard purchase(ServiceCard card);

    ServiceCard deduct(Long id);
}
