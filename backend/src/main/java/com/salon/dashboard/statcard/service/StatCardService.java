package com.salon.dashboard.statcard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salon.dashboard.statcard.entity.DashboardStatCard;
import com.salon.dashboard.statcard.mapper.DashboardStatCardMapper;
import com.salon.dashboard.statcard.model.StatCardSaveDTO;
import com.salon.dashboard.statcard.model.StatCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatCardService {

    private final DashboardStatCardMapper mapper;

    public List<StatCardVO> list() {
        List<DashboardStatCard> list = mapper.selectList(
                new LambdaQueryWrapper<DashboardStatCard>().orderByAsc(DashboardStatCard::getSlot));
        List<StatCardVO> vos = new ArrayList<>();
        for (DashboardStatCard e : list) {
            StatCardVO vo = new StatCardVO();
            vo.setSlot(e.getSlot());
            vo.setStatKey(e.getStatKey());
            vo.setLabel(e.getLabel());
            vo.setPath(e.getPath());
            vos.add(vo);
        }
        return vos;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<StatCardSaveDTO> dtoList) {
        mapper.delete(new LambdaQueryWrapper<>());
        for (StatCardSaveDTO dto : dtoList) {
            DashboardStatCard entity = new DashboardStatCard();
            entity.setSlot(dto.getSlot());
            entity.setStatKey(dto.getStatKey());
            entity.setLabel(dto.getLabel());
            entity.setPath(dto.getPath());
            int rows = mapper.insert(entity);
            if (rows == 0) throw new RuntimeException("保存统计卡片失败: slot=" + dto.getSlot());
        }
    }
}
