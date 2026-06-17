package com.salon.menuconfig.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salon.menuconfig.entity.MenuConfig;
import com.salon.menuconfig.mapper.MenuConfigMapper;
import com.salon.menuconfig.model.MenuConfigSaveDTO;
import com.salon.menuconfig.model.MenuConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuConfigService {

    private final MenuConfigMapper menuConfigMapper;

    public List<MenuConfigVO> list() {
        List<MenuConfig> list = menuConfigMapper.selectList(null);
        List<MenuConfigVO> vos = new ArrayList<>();
        for (MenuConfig e : list) {
            MenuConfigVO vo = new MenuConfigVO();
            vo.setMenuIndex(e.getMenuIndex());
            vo.setGroupName(e.getGroupName());
            vos.add(vo);
        }
        return vos;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<MenuConfigSaveDTO> dtoList) {
        menuConfigMapper.delete(new LambdaQueryWrapper<>());
        for (MenuConfigSaveDTO dto : dtoList) {
            MenuConfig entity = new MenuConfig();
            entity.setMenuIndex(dto.getMenuIndex());
            entity.setGroupName(dto.getGroupName());
            int rows = menuConfigMapper.insert(entity);
            if (rows == 0) throw new RuntimeException("保存菜单配置失败: " + dto.getMenuIndex());
        }
    }
}
