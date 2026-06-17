package com.salon.dashboard.quickaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salon.dashboard.quickaction.entity.DashboardQuickAction;
import com.salon.dashboard.quickaction.mapper.DashboardQuickActionMapper;
import com.salon.dashboard.quickaction.model.QuickActionSaveDTO;
import com.salon.dashboard.quickaction.model.QuickActionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuickActionService {

    private final DashboardQuickActionMapper mapper;

    public List<QuickActionVO> list() {
        List<DashboardQuickAction> list = mapper.selectList(
                new LambdaQueryWrapper<DashboardQuickAction>().orderByAsc(DashboardQuickAction::getSlot));
        List<QuickActionVO> vos = new ArrayList<>();
        for (DashboardQuickAction e : list) {
            QuickActionVO vo = new QuickActionVO();
            vo.setSlot(e.getSlot());
            vo.setLabel(e.getLabel());
            vo.setPath(e.getPath());
            vos.add(vo);
        }
        return vos;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<QuickActionSaveDTO> dtoList) {
        mapper.delete(new LambdaQueryWrapper<>());
        for (QuickActionSaveDTO dto : dtoList) {
            DashboardQuickAction entity = new DashboardQuickAction();
            entity.setSlot(dto.getSlot());
            entity.setLabel(dto.getLabel());
            entity.setPath(dto.getPath());
            int rows = mapper.insert(entity);
            if (rows == 0) throw new RuntimeException("保存快捷入口失败: slot=" + dto.getSlot());
        }
    }
}
