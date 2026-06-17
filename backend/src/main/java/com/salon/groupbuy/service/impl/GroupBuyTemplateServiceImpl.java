package com.salon.groupbuy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import com.salon.groupbuy.dto.GroupBuyTemplateReqDTO;
import com.salon.groupbuy.entity.GroupBuyTemplate;
import com.salon.groupbuy.mapper.GroupBuyTemplateMapper;
import com.salon.groupbuy.service.GroupBuyTemplateService;
import com.salon.groupbuy.vo.GroupBuyTemplatePageVO;
import com.salon.groupbuy.vo.GroupBuyTemplateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupBuyTemplateServiceImpl extends ServiceImpl<GroupBuyTemplateMapper, GroupBuyTemplate>
    implements GroupBuyTemplateService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Page<GroupBuyTemplatePageVO> page(int page, int size, String name, Integer status) {
        LambdaQueryWrapper<GroupBuyTemplate> w = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) w.like(GroupBuyTemplate::getName, name);
        if (status != null) w.eq(GroupBuyTemplate::getStatus, status);
        w.orderByAsc(GroupBuyTemplate::getSortOrder).orderByDesc(GroupBuyTemplate::getCreateTime);

        Page<GroupBuyTemplate> entityPage = baseMapper.selectPage(new Page<>(page, size), w);

        Page<GroupBuyTemplatePageVO> result = new Page<>(page, size, entityPage.getTotal());
        result.setRecords(entityPage.getRecords().stream().map(e -> {
            GroupBuyTemplatePageVO vo = new GroupBuyTemplatePageVO();
            vo.setId(e.getId());
            vo.setName(e.getName());
            vo.setImageUrl(e.getImageUrl());
            vo.setGroupPrice(e.getGroupPrice());
            vo.setOriginalPrice(e.getOriginalPrice());
            vo.setGroupSize(e.getGroupSize());
            vo.setExpireHours(e.getExpireHours());
            vo.setTotalQty(e.getTotalQty());
            vo.setIssuedQty(e.getIssuedQty());
            vo.setStatus(e.getStatus());
            vo.setStartTime(e.getStartTime());
            vo.setEndTime(e.getEndTime());
            vo.setCreateTime(e.getCreateTime());
            return vo;
        }).collect(Collectors.toList()));
        return result;
    }

    @Override
    public GroupBuyTemplateVO getById(Long id) {
        GroupBuyTemplate entity = baseMapper.selectById(id);
        if (entity == null) throw new BusinessException(ErrorCode.GROUP_BUY_TEMPLATE_NOT_FOUND);

        GroupBuyTemplateVO vo = new GroupBuyTemplateVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setImageUrl(entity.getImageUrl());
        vo.setDescription(entity.getDescription());
        vo.setOriginalPrice(entity.getOriginalPrice());
        vo.setGroupPrice(entity.getGroupPrice());
        vo.setGroupSize(entity.getGroupSize());
        vo.setExpireHours(entity.getExpireHours());
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setTotalQty(entity.getTotalQty());
        vo.setIssuedQty(entity.getIssuedQty());
        vo.setStatus(entity.getStatus());
        vo.setSortOrder(entity.getSortOrder());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    @Override
    public GroupBuyTemplateVO create(GroupBuyTemplateReqDTO dto) {
        int status = dto.getStatus() != null ? dto.getStatus() : 1;
        int sortOrder = dto.getSortOrder() != null ? dto.getSortOrder() : 0;
        int expireHours = dto.getExpireHours() != null ? dto.getExpireHours() : 24;
        int totalQty = dto.getTotalQty() != null ? dto.getTotalQty() : 0;

        int rows = jdbcTemplate.update(
            "INSERT INTO group_buy_template (name, image_url, description, original_price, group_price, group_size, expire_hours, start_time, end_time, total_qty, status, sort_order) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            dto.getName(), dto.getImageUrl(), dto.getDescription(),
            dto.getOriginalPrice(), dto.getGroupPrice(), dto.getGroupSize(),
            expireHours, dto.getStartTime(), dto.getEndTime(), totalQty, status, sortOrder);
        if (rows == 0) throw new BusinessException(ErrorCode.INTERNAL_ERROR, "拼团模板创建失败");

        List<Map<String, Object>> saved = jdbcTemplate.queryForList(
            "SELECT * FROM group_buy_template ORDER BY id DESC LIMIT 1");
        if (saved.isEmpty()) throw new BusinessException(ErrorCode.INTERNAL_ERROR, "拼团模板创建后查询失败");
        return mapRowToVO(saved.get(0));
    }

    @Override
    public GroupBuyTemplateVO update(Long id, GroupBuyTemplateReqDTO dto) {
        // Only allow editing templates without active orders (R6: issued_qty guard)
        int rows = jdbcTemplate.update(
            "UPDATE group_buy_template SET name = ?, image_url = ?, description = ?, original_price = ?, group_price = ?, group_size = ?, expire_hours = ?, start_time = ?, end_time = ?, total_qty = ?, status = ?, sort_order = ? WHERE id = ? AND issued_qty = 0",
            dto.getName(), dto.getImageUrl(), dto.getDescription(),
            dto.getOriginalPrice(), dto.getGroupPrice(), dto.getGroupSize(),
            dto.getExpireHours() != null ? dto.getExpireHours() : 24,
            dto.getStartTime(), dto.getEndTime(),
            dto.getTotalQty() != null ? dto.getTotalQty() : 0,
            dto.getStatus() != null ? dto.getStatus() : 1,
            dto.getSortOrder() != null ? dto.getSortOrder() : 0,
            id);
        if (rows == 0) {
            // Check whether template exists or has active orders
            GroupBuyTemplate existing = baseMapper.selectById(id);
            if (existing == null) throw new BusinessException(ErrorCode.GROUP_BUY_TEMPLATE_NOT_FOUND);
            throw new BusinessException(ErrorCode.GROUP_BUY_TEMPLATE_HAS_ORDERS);
        }

        Map<String, Object> row = jdbcTemplate.queryForMap(
            "SELECT * FROM group_buy_template WHERE id = ?", id);
        return mapRowToVO(row);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        int rows = jdbcTemplate.update(
            "UPDATE group_buy_template SET status = ? WHERE id = ? AND status != ?", status, id, status);
        if (rows == 0) throw new BusinessException(ErrorCode.GROUP_BUY_TEMPLATE_NOT_FOUND);
    }

    private GroupBuyTemplateVO mapRowToVO(Map<String, Object> row) {
        GroupBuyTemplateVO vo = new GroupBuyTemplateVO();
        vo.setId(((Number) row.get("id")).longValue());
        vo.setName((String) row.get("name"));
        vo.setImageUrl((String) row.get("image_url"));
        vo.setDescription((String) row.get("description"));
        vo.setOriginalPrice((BigDecimal) row.get("original_price"));
        vo.setGroupPrice((BigDecimal) row.get("group_price"));
        vo.setGroupSize(((Number) row.get("group_size")).intValue());
        vo.setExpireHours(((Number) row.get("expire_hours")).intValue());
        Object startTime = row.get("start_time");
        if (startTime instanceof LocalDateTime ldt) vo.setStartTime(ldt);
        else if (startTime instanceof java.sql.Timestamp ts) vo.setStartTime(ts.toLocalDateTime());
        Object endTime = row.get("end_time");
        if (endTime instanceof LocalDateTime ldt) vo.setEndTime(ldt);
        else if (endTime instanceof java.sql.Timestamp ts) vo.setEndTime(ts.toLocalDateTime());
        vo.setTotalQty(((Number) row.get("total_qty")).intValue());
        vo.setIssuedQty(((Number) row.get("issued_qty")).intValue());
        vo.setStatus(((Number) row.get("status")).intValue());
        vo.setSortOrder(((Number) row.get("sort_order")).intValue());
        Object createTime = row.get("create_time");
        if (createTime instanceof LocalDateTime ldt) vo.setCreateTime(ldt);
        else if (createTime instanceof java.sql.Timestamp ts) vo.setCreateTime(ts.toLocalDateTime());
        Object updateTime = row.get("update_time");
        if (updateTime instanceof LocalDateTime ldt) vo.setUpdateTime(ldt);
        else if (updateTime instanceof java.sql.Timestamp ts) vo.setUpdateTime(ts.toLocalDateTime());
        return vo;
    }
}
