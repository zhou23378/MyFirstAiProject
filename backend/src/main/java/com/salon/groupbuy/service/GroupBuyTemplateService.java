package com.salon.groupbuy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.groupbuy.dto.GroupBuyTemplateReqDTO;
import com.salon.groupbuy.entity.GroupBuyTemplate;
import com.salon.groupbuy.vo.GroupBuyTemplatePageVO;
import com.salon.groupbuy.vo.GroupBuyTemplateVO;

public interface GroupBuyTemplateService extends IService<GroupBuyTemplate> {

    Page<GroupBuyTemplatePageVO> page(int page, int size, String name, Integer status);

    GroupBuyTemplateVO getById(Long id);

    GroupBuyTemplateVO create(GroupBuyTemplateReqDTO dto);

    GroupBuyTemplateVO update(Long id, GroupBuyTemplateReqDTO dto);

    void updateStatus(Long id, Integer status);
}
